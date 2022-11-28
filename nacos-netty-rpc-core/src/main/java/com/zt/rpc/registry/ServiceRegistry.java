package com.zt.rpc.registry;

import com.zt.rpc.dto.RegisterInfo;

public interface ServiceRegistry {

    void register(String serviceName, RegisterInfo registerInfo);

    void deregister(String serviceName, RegisterInfo registerInfo);

}
