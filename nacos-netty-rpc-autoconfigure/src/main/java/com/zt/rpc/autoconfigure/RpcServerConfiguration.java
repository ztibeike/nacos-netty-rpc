package com.zt.rpc.autoconfigure;

import com.zt.rpc.autoconfigure.RpcProperties;
import com.zt.rpc.registry.ServiceRegistry;
import com.zt.rpc.serializer.Serializer;
import com.zt.rpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcServerConfiguration {

    @Autowired
    private RpcProperties rpcProperties;

    @Autowired
    private Serializer serializer;

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(
                rpcProperties.getApplication().getName(),
                rpcProperties.getServer().getIp(),
                rpcProperties.getServer().getPort(),
                serializer,
                serviceRegistry
        );

    }

}
