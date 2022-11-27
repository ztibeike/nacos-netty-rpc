package com.zt.rpc.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RpcProperties.class})
public class EnablePropertiesAutoConfiguration {
}
