package com.zt.rpc.autoconfigure;

import com.zt.rpc.util.NetworkUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rpc")
@Getter
@Setter
public class RpcProperties {

    private RpcApplicationProperties application = new RpcApplicationProperties();

    private RpcServerProperties server = new RpcServerProperties();

    private NacosProperties nacos = new NacosProperties();


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RpcApplicationProperties {
        private String name = "demo-service";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RpcServerProperties {
        private String ip = NetworkUtil.getLocalIpAddress();
        private Integer port;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NacosProperties {
        private String address;
        private String namespace = "public";
    }

}
