package com.zt.rpc.autoconfigure;

import com.zt.rpc.registry.ServiceDiscovery;
import com.zt.rpc.registry.ServiceRegistry;
import com.zt.rpc.registry.nacos.NacosServiceDiscovery;
import com.zt.rpc.registry.nacos.NacosServiceRegistry;
import com.zt.rpc.serializer.DefaultSerializer;
import com.zt.rpc.serializer.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcAutoConfiguration {

    @Autowired
    private RpcProperties rpcProperties;

    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    public Serializer serializer() {
        return new DefaultSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry serviceRegistry() {
        return new NacosServiceRegistry(rpcProperties.getNacos().getAddress(), rpcProperties.getNacos().getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    public ServiceDiscovery serviceDiscovery() {
        return new NacosServiceDiscovery(rpcProperties.getNacos().getAddress(), rpcProperties.getNacos().getNamespace());
    }

}
