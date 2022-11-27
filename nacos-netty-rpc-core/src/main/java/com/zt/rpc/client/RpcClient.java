package com.zt.rpc.client;

import com.zt.rpc.registry.ServiceDiscovery;
import com.zt.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

@Slf4j
public class RpcClient implements DisposableBean {

    private ConnectionHolder connectionHolder;

    public RpcClient(ServiceDiscovery serviceDiscovery, Serializer serializer) {
        this.connectionHolder = ConnectionHolder.getInstance();
        connectionHolder.setServiceDiscovery(serviceDiscovery);
        connectionHolder.setSerializer(serializer);
    }

    @Override
    public void destroy() throws Exception {
        this.connectionHolder.stop();
    }
}
