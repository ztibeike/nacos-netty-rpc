package com.zt.rpc.server;

import com.zt.rpc.dto.IdleBeat;
import com.zt.rpc.dto.RpcRequest;
import com.zt.rpc.dto.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Map<String, Object> serviceMap;

    private ThreadPoolExecutor executor;

    public RpcServerHandler(Map<String, Object> serviceMap, ThreadPoolExecutor executor) {
        this.serviceMap = serviceMap;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        if (IdleBeat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())) {
            log.info("Server receive heart beat from client");
            return;
        }
        executor.execute(() -> {
            log.info("Server receive request: {}", rpcRequest.getRequestId());
            RpcResponse response = RpcResponse.builder().requestId(rpcRequest.getRequestId()).build();
            try {
                Object result = handle(rpcRequest);
                response.setResult(result);
            } catch (Exception e) {
                response.setError(e.toString());
                log.error("RPC Server failed to handle request: {}", rpcRequest);
            }
            channelHandlerContext.writeAndFlush(response).addListener((ChannelFuture channelFuture) -> log.info("Send response for request: {}", rpcRequest.getRequestId()));
        });
    }

    private Object handle(RpcRequest request) throws InvocationTargetException, NoSuchMethodException {
        String serviceName = request.getClassName();
        String methodName = request.getMethodName();
        String requestId = request.getRequestId();
        Object serviceBean = this.serviceMap.get(serviceName);
        if (serviceBean == null) {
            log.error("cannot find service handler with name: {}", serviceName);
            return null;
        }
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        if (parameterTypes.length != parameters.length) {
            log.error("Inconsistency in numbers of parameters and parameterTypes of request: {}", requestId);
            return null;
        }
        Class<?> clazz = serviceBean.getClass();
        log.debug("Find handler [{}] for request: {}", clazz.getName(), requestId);
        FastClass fastClass = FastClass.create(clazz);
        int methodIdx = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIdx, serviceBean, parameters);
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            log.warn("Channel idle time exceed {} seconds, closed.", IdleBeat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Server caught exception: {}", cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }
}
