package org.demo.rpc;

import com.zt.rpc.annotation.RpcClientService;

@RpcClientService(service = "test-service", handler = "HelloService")
public interface RpcHelloService {
    String hello();
}
