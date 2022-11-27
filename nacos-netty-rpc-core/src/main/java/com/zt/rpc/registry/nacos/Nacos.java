package com.zt.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public abstract class Nacos {

    protected String nacosServerAddr;

    protected String nacosNamespace;

    protected NamingService namingService;

    Nacos(String nacosServerAddr, String nacosNamespace) {
        this.nacosServerAddr = nacosServerAddr;
        this.nacosNamespace = nacosNamespace;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosServerAddr);
            properties.put("namespace", nacosNamespace);
            this.namingService = NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("Failed to create nacos naming service");
            log.debug(e.toString());
        }
    }
}
