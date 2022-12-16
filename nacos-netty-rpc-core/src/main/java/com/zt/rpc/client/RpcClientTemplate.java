package com.zt.rpc.client;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.zt.rpc.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class RpcClientTemplate {

    public RpcClientFuture request(String service, String handler, String method, Object ...params) {
        RpcRequest request = RpcRequest.builder()
                .requestId(UuidUtils.generateUuid())
                .parameters(params)
                .parameterTypes(Arrays.stream(params).map(Object::getClass).toArray(Class[]::new))
                .className(handler)
                .methodName(method)
                .build();
        RpcClientHandler rpcClientHandler = ConnectionHolder.getInstance().getHandler(service);
        return rpcClientHandler.sendRequest(request);
    }
}
