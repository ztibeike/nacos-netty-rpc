package com.zt.rpc.client;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zt.rpc.dto.IdleBeat;
import com.zt.rpc.dto.RpcRequest;
import com.zt.rpc.dto.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final Map<String, RpcClientFuture> pendingRpc = new ConcurrentHashMap<>();

    private volatile Channel channel;

    private SocketAddress socketAddress;

    @Setter
    private Instance instance;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        log.debug("Receive response: {}", requestId);
        RpcClientFuture future = this.pendingRpc.get(requestId);
        if (future != null) {
            future.done(response);
            pendingRpc.remove(requestId);
        } else {
            log.warn("Receive unused response: {}", requestId);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.socketAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            sendRequest(IdleBeat.BEAT_PING);
            log.debug("Send idle beat to server {}", socketAddress.toString());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client caught exception: {}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionHolder.getInstance().removeAndCloseHandler(this.instance);
    }

    public RpcClientFuture sendRequest(RpcRequest request) {
        RpcClientFuture future = new RpcClientFuture(request);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if (channelFuture.isSuccess()) {
                pendingRpc.put(request.getRequestId(), future);
            } else {
                log.error("Failed to send request {}", request.getRequestId());
            }
        } catch (InterruptedException e) {
            log.error("Failed to send request {}", request.getRequestId());
        }
        return future;
    }

    public void close() {
        this.channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

}
