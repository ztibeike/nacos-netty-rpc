package org.demo;

import org.demo.rpc.RpcHelloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RpcClientTest {

    @Autowired
    private RpcHelloService helloService;

    @Test
    public void test() {
        System.out.println(helloService.hello());
        System.out.println(helloService.helloWithName("ZT"));
    }

}
