# Nacos Netty Rpc

一个基于Nacos、Netty、SpringBoot的RPC框架

### 特点

- 基于Nacos实现服务注册、发现和负载均衡
- 支持心跳机制检测TCP连接状态
- 支持不同的序列化工具，默认为ProtoStuff
- 提供注册中心和序列化工具的扩展接口
- 支持类似于OpenFeign的FeignClient接口方式的声明式调用

### 使用方式

#### RPC Server

1. 定义服务

   ```Java
   @RpcServerService
   public class HelloService {
       public String hello(String name) {
           return "hello " + name;
       }
   }
   ```

2. 使用注解@EnableRpcServer开启RPC服务端

   ```java
   @SpringBootApplication
   @EnableRpcServer
   public class RpcServerDemoApplication {
       public static void main(String[] args) {
           SpringApplication.run(RpcServerDemoApplication.class, args);
       }
   }
   ```

3. 添加配置

   ```yaml
   # application.yml
   rpc:
     nacos:
       address: nacos.server # 你的nacos server的域名(默认8848端口)，或{IP}:{Port}
     application:
       name: rpc-server-demo-service
     server:
       port: 7890
   ```

   

#### RPC Client

1. 定义远程服务接口

   方式1：接口名与远程服务的类名一致

   ```java
   @RpcClientService(service = "rpc-server-demo-service")
   public interface HelloService {
       String hello(String name);
   }
   ```

   方式2：使用handler参数指定远程服务的类名

   ```java
   @RpcClientService(service = "rpc-server-demo-service", handler = "HelloService")
   public interface XxxxxService {
       String hello(String name);
   }
   ```

2. 使用注解@EnableRpcClient开启客户端

   ```java
   @SpringBootApplication
   @EnableRpcClient
   public class RpcClientDemoApplication {
       public static void main(String[] args) {
           SpringApplication.run(RpcClientDemoApplication.class, args);
       }
   }
   ```

3. 添加配置

   ```yaml
   # application.yml
   rpc:
     nacos:
       address: nacos.server # 你的nacos server的域名(默认8848端口)，或{IP}:{Port}4
   ```

4. 调用远程服务

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

### 示例项目

仓库链接：[Gitee](https://gitee.com/zengtao321/nacos-netty-rpc-demo) [GitHub](https://github.com/ztibeike/nacos-netty-rpc-demo)

