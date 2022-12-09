package org.demo.service;

import com.zt.rpc.annotation.RpcServerService;

@RpcServerService
public class HelloService {

    public String hello() {
        return "hello";
    }

    public String helloWithName(String name) {
        return "Hello " + name + "!";
    }

}
