package com.zt.rpc.reflect;

import org.springframework.stereotype.Component;

@Component
public class TestHelloService {
    public void sayHello() {
        System.out.println("Hello!");
    }
}
