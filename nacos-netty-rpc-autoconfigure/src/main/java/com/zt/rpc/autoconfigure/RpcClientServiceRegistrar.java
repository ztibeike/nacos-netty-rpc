package com.zt.rpc.autoconfigure;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

public class RpcClientServiceRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry);
        String[] basePackages = this.getBasePackages(importingClassMetadata);
        RpcClientServiceScanner scanner = new RpcClientServiceScanner(registry, false);
        scanner.doScan(basePackages);
    }

    private String[] getBasePackages(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRpcClient.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        if (ArrayUtil.isEmpty(basePackages)) {
            basePackages = new String[]{ClassUtils.getPackageName(importingClassMetadata.getClassName())};
        }
        return basePackages;
    }
}
