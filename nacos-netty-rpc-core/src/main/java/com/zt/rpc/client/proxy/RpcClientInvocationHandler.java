package com.zt.rpc.client.proxy;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.zt.rpc.client.ConnectionHolder;
import com.zt.rpc.client.RpcClientFuture;
import com.zt.rpc.client.RpcClientHandler;
import com.zt.rpc.dto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcClientInvocationHandler implements InvocationHandler {

    private String serviceName;

    private String handlerName;

    public RpcClientInvocationHandler(String serviceName, String handlerName) {
        this.serviceName = serviceName;
        this.handlerName = handlerName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(proxy, args);
        }
        RpcRequest request = RpcRequest.builder()
                .requestId(UuidUtils.generateUuid())
                .className(this.handlerName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();
        RpcClientHandler handler = ConnectionHolder.getInstance().getHandler(serviceName);
        RpcClientFuture future = handler.sendRequest(request);
        return future.get();
    }
}
