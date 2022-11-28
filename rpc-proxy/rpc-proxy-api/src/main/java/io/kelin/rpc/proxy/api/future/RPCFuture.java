package io.kelin.rpc.proxy.api.future;

import io.kelin.rpc.common.threadpool.ClientThreadPool;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.kelin.rpc.proxy.api.callback.AsyncRPCCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @version  1.0.0
 * @description RPC框架获取异步结果的自定义Future
 */
public class RPCFuture extends CompletableFuture<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;

    private long responseTimeThreshold = 5000;

    //异步回调用
    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol){
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if(this.responseRpcProtocol != null){
            return this.responseRpcProtocol.getBody().getResult();
        }else{
            return null;
        }
    }



    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if(success){
            if(this.responseRpcProtocol != null){
                return this.responseRpcProtocol.getBody().getResult();
            }else {
                return null;
            }
        }else{
            throw new RuntimeException("Timeout exception. Request id: "+this.requestRpcProtocol.getHeader().getRequestId()
                                  +". Request class name: " + this.requestRpcProtocol.getBody().getClassName()
                                  +". Request method: " + this.requestRpcProtocol.getBody().getMethodName());
        }

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    public void done(RpcProtocol<RpcResponse> responseRpcProtocol){
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);

        //触发回调
        invokeCallbacks();
        //Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if(responseTime > this.responseTimeThreshold){
            LOGGER.warn("Service response time is too slow. Request id = "
                   + responseRpcProtocol.getHeader().getRequestId()
                   + ". Request time = " + responseTime);
        }
    }

    /**
     * 依次触发pendingCallbacks集合中的回调接口的方法
     */
    private void invokeCallbacks(){
        lock.lock();
        try{
            for( final AsyncRPCCallback callback : pendingCallbacks){
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 将回调接口实例对象添加到pendingCallbacks
     * @param callback
     * @return
     */
    public RPCFuture addCallback(AsyncRPCCallback callback){
        lock.lock();
        try{
            if(isDone()){
                runCallback(callback);
            }else{
                this.pendingCallbacks.add(callback);
            }
        }finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 收到响应后，将回调推入线程池，异步执行
     * @param callback
     */
    private void runCallback(final AsyncRPCCallback callback){
        final RpcResponse res = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(()->{
            if(!res.isError()){
                callback.onSuccess(res.getResult());
            }else{
                callback.onException(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }



    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID= 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int acquires) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int releases) {
            if(getState() == pending){
                if(compareAndSetState(pending, done)){
                    return true;
                }
            }
            return false;
        }

        public boolean isDone(){
            return getState() == done;
        }
    }
}
