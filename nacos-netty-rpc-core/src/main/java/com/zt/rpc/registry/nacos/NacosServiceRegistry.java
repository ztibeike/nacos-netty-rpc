package com.zt.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zt.rpc.dto.RegisterInfo;
import com.zt.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NacosServiceRegistry extends Nacos implements ServiceRegistry {

    public NacosServiceRegistry(String nacosServerAddr, String nacosNamespace) {
        super(nacosServerAddr, nacosNamespace);
    }

    @Override
    public void register(String serviceName, RegisterInfo registerInfo) {
        Instance instance = new Instance();
        instance.setServiceName(serviceName);
        instance.setIp(registerInfo.getIp());
        instance.setPort(registerInfo.getPort());
        Map<String, String> metadata = new HashMap<>();
        metadata.put("services", registerInfo.servicesToString());
        instance.setMetadata(metadata);
        try {
            this.namingService.registerInstance(serviceName, instance);
            log.info("Successfully register instance {}", instance);
        } catch (NacosException e) {
            log.error("Failed to register instance {}", instance);
            log.debug(e.toString());
        }
    }

    @Override
    public void deregister(String serviceName, RegisterInfo registerInfo) {
        try {
            this.namingService.deregisterInstance(serviceName, registerInfo.getIp(), registerInfo.getPort());
        } catch (NacosException e) {
            log.error("Failed to deregister instance {}", registerInfo);
            log.debug(e.toString());
        }
    }
}
