package com.zt.rpc.client.proxy;

import com.zt.rpc.annotation.RpcClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClientFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceClass;

    private String serviceName;

    private String handlerName;

    public RpcClientFactoryBean(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
        RpcClientService rpcClientService = AnnotationUtils.findAnnotation(interfaceClass, RpcClientService.class);
        if (rpcClientService == null) {
            log.error("No @RpcClientService is found for {}", interfaceClass.getName());
            return;
        }
        if (!StringUtils.hasText(rpcClientService.service())) {
            log.error("@RpcClientService is found for {}, but without service specified", interfaceClass.getName());
            return;
        }
        this.serviceName = rpcClientService.service();
        this.handlerName = StringUtils.hasText(rpcClientService.handler()) ? rpcClientService.handler() : interfaceClass.getSimpleName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        if (!StringUtils.hasText(this.serviceName)) {
            throw new RuntimeException("No serviceName specified");
        }
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new RpcClientInvocationHandler(this.serviceName, this.handlerName)
        );
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }
}
