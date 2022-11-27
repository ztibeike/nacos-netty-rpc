package com.zt.rpc.registry;

public interface ServiceDiscovery {

    <T> T selectOneInstance(String serviceName, Class<T> clazz);

    <FN> void subscribe(String serviceName, FN listener);

    <FN> void unsubscribe(String serviceName, FN listener);

}
