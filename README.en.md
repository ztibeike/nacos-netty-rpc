1. # Nacos Netty Rpc

   An RPC framework based on Nacos, Netty and SpringBoot

   ### Features

   - Service registry, discovery and load balancing are supported by Nacos
   - Support the heartbeat mechanism to detect TCP connection status
   - Support different serialization tools, default is ProtoStuff
   - Provide extension interfaces to the service registry and serialization tool
   - Support declarative invocations similar to OpenFeign's FeignClient interface

   ### How to use

   #### RPC Server

   1. Define a service

      ```Java
      @RpcServerService
      public class HelloService {
          public String hello(String name) {
              return "hello " + name;
          }
      }
      ```

   2. Enable the RPC server with the annotation @EnableRpcServer

      ```java
      @SpringBootApplication
      @EnableRpcServer
      public class RpcServerDemoApplication {
          public static void main(String[] args) {
              SpringApplication.run(RpcServerDemoApplication.class, args);
          }
      }
      ```

   3. Add configuration

      ```yaml
      # application.yml
      rpc:
        nacos:
          address: nacos.server # Domain name of your nacos server (default Port 8848), or {IP}:{Port}
        application:
          name: rpc-server-demo-service
        server:
          port: 7890
      ```

      

   #### RPC Client

   1. Define the remote service interface

      Method 1: The interface name is the same as the remote service class name

      ```java
      @RpcClientService(service = "rpc-server-demo-service")
      public interface HelloService {
          String hello(String name);
      }
      ```

      Method 2: Use the handler parameter to specify the class name of the remote service

      ```java
      @RpcClientService(service = "rpc-server-demo-service", handler = "HelloService")
      public interface XxxxxService {
          String hello(String name);
      }
      ```

   2. Enable the client with the annotation @EnableRpcClient

      ```java
      @SpringBootApplication
      @EnableRpcClient
      public class RpcClientDemoApplication {
          public static void main(String[] args) {
              SpringApplication.run(RpcClientDemoApplication.class, args);
          }
      }
      ```

   3. Add configuration

      ```yaml
      # application.yml
      rpc:
        nacos:
          address: nacos.server # Domain name of your nacos server (default Port 8848), or {IP}:{Port}
      ```

   4. Call the remote service

      ```java
      @SpringBootTest
      public class TestRpcClient {
      
          @Autowired
          private HelloService helloService;
      
          @Test
          public void test() {
              System.out.println(helloService.hello("ZT"));
          }
      
      }
      ```

### Demo

Quick reference：[Gitee](https://gitee.com/zengtao321/nacos-netty-rpc-demo) [GitHub](https://github.com/ztibeike/nacos-netty-rpc-demo)

