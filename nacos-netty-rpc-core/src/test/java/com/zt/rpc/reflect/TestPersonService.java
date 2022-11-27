package com.zt.rpc.reflect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestPersonService {

    @Autowired
    private TestHelloService testHelloService;

    public void say() {
        testHelloService.sayHello();
    }


}
