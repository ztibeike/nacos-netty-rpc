package com.zt.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zt.rpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NacosServiceDiscovery extends Nacos implements ServiceDiscovery {

    public NacosServiceDiscovery(String nacosServerAddr, String nacosNamespace) {
        super(nacosServerAddr, nacosNamespace);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectOneInstance(String serviceName, Class<T> clazz) {
        if (!clazz.equals(Instance.class)) {
            log.debug("Failed to select a instance of service {}, caused by unsupported type {}", serviceName, clazz.getSimpleName());
            return null;
        }
        Instance instance = null;
        try {
            instance = this.namingService.selectOneHealthyInstance(serviceName);
        } catch (NacosException e) {
            log.error("Failed to select a instance of service {}", serviceName);
            log.debug(e.toString());
        }
        return (T) instance;
    }

    @Override
    public <FN> void subscribe(String serviceName, FN listener) {
        if (!(listener instanceof EventListener)) {
            log.debug("Failed to subscribe service {}, cause by unsupported event listener type {}", serviceName, listener.getClass().getSimpleName());
            return;
        }
        try {
            this.namingService.subscribe(serviceName, (EventListener) listener);
        } catch (NacosException e) {
            log.error("Failed to subscribe service {}", serviceName);
            log.debug(e.toString());
        }
    }

    @Override
    public <FN> void unsubscribe(String serviceName, FN listener) {
        if (!(listener instanceof EventListener)) {
            log.debug("Failed to unsubscribe service {}, cause by unsupported event listener type {}", serviceName, listener.getClass().getSimpleName());
            return;
        }
        try {
            this.namingService.unsubscribe(serviceName, (EventListener) listener);
        } catch (NacosException e) {
            log.error("Failed to unsubscribe service {}", serviceName);
            log.debug(e.toString());
        }
    }
}
