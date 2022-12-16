package com.zt.rpc.client;

import com.zt.rpc.dto.RpcRequest;
import com.zt.rpc.dto.RpcResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@Slf4j
public class RpcClientFuture implements Future<Object> {

    @Getter
    @Setter
    private RpcRequest request;

    private RpcResponse response;

    private RpcSync sync;

    private RpcClientCallBack callBack;

    public RpcClientFuture(RpcRequest request) {
        this.request = request;
        this.sync = new RpcSync();
    }

    public void done(RpcResponse response) {
        this.response = response;
        invokeCallBackSuccess(response);
        this.sync.release(1);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() {
        sync.acquire(1);
        return this.response == null ? null : response.getResult();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean ok = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (!ok) {
            throw new RuntimeException("Request timeout");
        }
        return this.response == null ? null : response.getResult();
    }

    public Object sync() {
        return this.get();
    }

    public Object sync(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException  {
        return this.get(timeout, unit);
    }

    public RpcClientFuture async() {
        return this;
    }

    public RpcClientFuture async(RpcClientCallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    private void invokeCallBackSuccess(RpcResponse response) {
        if (this.callBack != null) {
            this.callBack.success(response);
        }
    }

    private void invokeCallBackError(Throwable e) {
        if (this.callBack != null) {
            this.callBack.error(e);
        }
    }

    static class RpcSync extends AbstractQueuedSynchronizer {

        private final int DONE = 1;

        private final int PENDING = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == DONE;
        }

        @Override
        protected boolean tryRelease(int arg) {
            return getState() != PENDING || compareAndSetState(PENDING, DONE);
        }

        protected boolean isDone() {
            return getState() == DONE;
        }

    }

}
