package com.zt.rpc.server;

import cn.hutool.core.thread.ExecutorBuilder;
import com.alibaba.nacos.common.utils.MapUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.zt.rpc.annotation.RpcServerService;
import com.zt.rpc.codec.*;
import com.zt.rpc.dto.IdleBeat;
import com.zt.rpc.dto.RegisterInfo;
import com.zt.rpc.dto.RpcRequest;
import com.zt.rpc.dto.RpcResponse;
import com.zt.rpc.registry.ServiceRegistry;
import com.zt.rpc.serializer.Serializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    private final String serviceName;

    private final String ip;

    private final Integer port;

    private final Serializer serializer;

    private final ServiceRegistry serviceRegistry;

    private final ThreadPoolExecutor executor;

    private final Map<String, Object> serviceMap = new HashMap<>();

    private Thread thread;

    /**
     *
     * @param serviceName 服务名称
     * @param ip 服务实例IP
     * @param port 服务实例端口
     * @param serializer 对象序列化工具
     * @param serviceRegistry 服务注册中心
     */
    public RpcServer(String serviceName, String ip, Integer port, Serializer serializer, ServiceRegistry serviceRegistry) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.serializer = serializer;
        this.serviceRegistry = serviceRegistry;
        this.executor = ExecutorBuilder.create().setCorePoolSize(8).setMaxPoolSize(16)
                .setKeepAliveTime(64L, TimeUnit.SECONDS).setWorkQueue(new LinkedBlockingDeque<>(1000))
                .setThreadFactory((Runnable r) -> new Thread(r, "nacos-netty-rpc-" + RpcServer.class.getSimpleName() + "-" + r.hashCode()))
                .build();;
    }

    private void start() {
        thread = new Thread(() -> {
            RegisterInfo registerInfo = new RegisterInfo(this.ip, this.port, new ArrayList<>(serviceMap.keySet()));
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline cp = socketChannel.pipeline();
                                cp.addLast(new IdleStateHandler(0, 0, IdleBeat.BEAT_TIMEOUT, TimeUnit.SECONDS));
                                cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                                cp.addLast(new RpcDecoder(serializer, RpcRequest.class));
                                cp.addLast(new RpcEncoder(serializer, RpcResponse.class));
                                cp.addLast(new RpcServerHandler(serviceMap, executor));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = bootstrap.bind(this.ip, this.port).sync();
                this.serviceRegistry.register(this.serviceName, registerInfo);
                log.info("Server started and listening on {}:{}", this.ip, this.port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.info("Rpc server stopped");
            } finally {
                this.serviceRegistry.deregister(serviceName, registerInfo);
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
        thread.start();
    }

    private void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Override
    public void destroy() throws Exception {
        this.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> rpcServices = applicationContext.getBeansWithAnnotation(RpcServerService.class);
        if (!MapUtil.isEmpty(rpcServices)) {
            for (Object serviceBean : rpcServices.values()) {
                RpcServerService rpcServerService = serviceBean.getClass().getAnnotation(RpcServerService.class);
                String serviceName = rpcServerService.value();
                if (StringUtils.isEmpty(serviceName)) {
                    serviceName = serviceBean.getClass().getSimpleName();
                }
                this.serviceMap.put(serviceName, serviceBean);
                log.info("Add service: {}", serviceName);
            }
        }
    }
}
