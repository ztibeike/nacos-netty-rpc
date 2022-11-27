package com.zt.rpc.client;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zt.rpc.codec.RpcDecoder;
import com.zt.rpc.codec.RpcEncoder;
import com.zt.rpc.dto.IdleBeat;
import com.zt.rpc.dto.RpcRequest;
import com.zt.rpc.dto.RpcResponse;
import com.zt.rpc.registry.ServiceDiscovery;
import com.zt.rpc.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionHolder {

    private final Map<String, RpcClientHandler> handlerMap;

    private final Map<String, EventListener> listenerMap;

    private static final ReentrantLock lock = new ReentrantLock();

    private static volatile ConnectionHolder instance = null;

    private EventLoopGroup eventLoopGroup;

    @Setter
    private ServiceDiscovery serviceDiscovery;

    @Setter
    private Serializer serializer;

    private ConnectionHolder() {
        this.handlerMap = new ConcurrentHashMap<>();
        this.listenerMap = new ConcurrentHashMap<>();
        this.eventLoopGroup = new NioEventLoopGroup(4);
    }

    public static ConnectionHolder getInstance() {
        if (instance == null) {
            lock.lock();
            if (instance == null) {
                instance = new ConnectionHolder();
            }
            lock.unlock();
        }
        return instance;
    }

    public RpcClientHandler getHandler(String serviceName) {
        Instance instance = this.serviceDiscovery.selectOneInstance(serviceName, Instance.class);
        if (instance == null) {
            return null;
        }
        String handlerKey = getHandlerKey(instance);
        if (this.handlerMap.containsKey(handlerKey)) {
            return this.handlerMap.get(handlerKey);
        }
        RpcClientHandler handler = null;
        try {
            handler = this.newHandler(instance);
            if (handler == null) {
                throw new RuntimeException();
            }
            this.handlerMap.put(handlerKey, handler);
            this.registerListeners(serviceName);
        } catch (Exception e) {
            log.error("Failed to get handler for instance: {}", instance);
        }
        return handler;
    }

    private void registerListeners(String serviceName) {
        if (this.listenerMap.containsKey(serviceName)) {
            return;
        }
        EventListener eventListener = (Event event) -> {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                List<Instance> instances = namingEvent.getInstances();
                Set<String> keys = this.handlerMap.keySet();
                keys.forEach(key -> {
                    if (key.startsWith(serviceName)) {
                        Instance instance = getInstanceFromHandlerKey(key);
                        boolean ok = instances.stream().anyMatch(inst ->
                                inst.getServiceName().equals(instance.getServiceName())
                                        && inst.getIp().equals(instance.getIp())
                                        && inst.getPort() == instance.getPort()
                        );
                        if (!ok) {
                            removeAndCloseHandler(key);
                        }
                    }
                });
            }
        };
        this.serviceDiscovery.subscribe(serviceName, eventListener);
        this.listenerMap.put(serviceName, eventListener);
    }

    private String getHandlerKey(Instance instance) {
        return instance.getServiceName() + ":" + instance.getIp() + ":" + instance.getPort();
    }

    private Instance getInstanceFromHandlerKey(String key) {
        String[] attrs = key.split(":");
        Instance instance = new Instance();
        instance.setServiceName(attrs[0]);
        instance.setIp(attrs[1]);
        instance.setPort(Integer.parseInt(attrs[2]));
        return instance;
    }

    private RpcClientHandler newHandler(Instance instance) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline cp = socketChannel.pipeline();
                cp.addLast(new IdleStateHandler(0, 0, IdleBeat.BEAT_INTERVAL, TimeUnit.SECONDS));
                cp.addLast(new RpcEncoder(serializer, RpcRequest.class));
                cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                cp.addLast(new RpcDecoder(serializer, RpcResponse.class));
                cp.addLast(new RpcClientHandler());
            }
        });
        SocketAddress address = new InetSocketAddress(instance.getIp(), instance.getPort());
        ChannelFuture channelFuture = bootstrap.connect(address);
        AtomicReference<RpcClientHandler> handler = new AtomicReference<>();
        channelFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                log.info("Successfully connected to rpc server: {} {}:{}", instance.getServiceName(), instance.getIp(), instance.getPort());
                RpcClientHandler clientHandler = future.channel().pipeline().get(RpcClientHandler.class);
                clientHandler.setInstance(instance);
                handler.set(clientHandler);
            } else {
                log.error("Failed to connect to rpc server: {} {}:{}", instance.getServiceName(), instance.getIp(), instance.getPort());
            }
            countDownLatch.countDown();
        });
        countDownLatch.await();
        return handler.get();
    }

    private void removeAndCloseHandler(String key) {
        RpcClientHandler handler = this.handlerMap.get(key);
        if (handler != null) {
            handler.close();
        }
    }

    public void removeAndCloseHandler(Instance instance) {
        this.removeAndCloseHandler(getHandlerKey(instance));
    }

    public void stop() {
        this.unregisterAllListeners();
        this.closeAllHandlers();
        this.eventLoopGroup.shutdownGracefully();
    }

    private void closeAllHandlers() {
        for (RpcClientHandler handler : this.handlerMap.values()) {
            handler.close();
        }
    }

    private void unregisterAllListeners() {
        for (String serviceName : this.listenerMap.keySet()) {
            this.serviceDiscovery.unsubscribe(serviceName, this.listenerMap.get(serviceName));
        }
    }

}
