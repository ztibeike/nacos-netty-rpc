package com.zt.rpc.client;

import com.zt.rpc.dto.RpcResponse;

public interface RpcClientCallBack {
    void success(RpcResponse response);

    void error(Throwable e);

}
