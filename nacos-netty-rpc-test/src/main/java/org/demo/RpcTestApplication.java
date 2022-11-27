package org.demo;

import com.zt.rpc.autoconfigure.EnableRpcClient;
import com.zt.rpc.autoconfigure.EnableRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcServer
@EnableRpcClient
public class RpcTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcTestApplication.class, args);
    }
}
