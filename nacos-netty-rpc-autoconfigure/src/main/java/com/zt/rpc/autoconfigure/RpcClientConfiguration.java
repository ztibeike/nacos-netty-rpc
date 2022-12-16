package com.zt.rpc.autoconfigure;

import com.zt.rpc.client.RpcClient;
import com.zt.rpc.client.RpcClientTemplate;
import com.zt.rpc.registry.ServiceDiscovery;
import com.zt.rpc.serializer.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcClientConfiguration {

    @Autowired
    private Serializer serializer;

    @Autowired
    private ServiceDiscovery serviceDiscovery;

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(serviceDiscovery, serializer);
    }

    @Bean
    public RpcClientTemplate rpcClientTemplate() {
        return new RpcClientTemplate();
    }

}
