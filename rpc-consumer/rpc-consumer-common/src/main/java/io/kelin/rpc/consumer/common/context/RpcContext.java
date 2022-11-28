package io.kelin.rpc.consumer.common.context;


import io.kelin.rpc.proxy.api.future.RPCFuture;

/**
 * @version 1.0.0
 * @description 保存PRC上下弯
 */
public class RpcContext {

    private RpcContext(){

    }

    /**
     * RpcContext实例
     */
    private static final RpcContext AGENT = new RpcContext();

    private static final InheritableThreadLocal<RPCFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取上下文
     * @return  RPC服务的上下文信息
     */
    public static RpcContext getContext(){
        return AGENT;
    }

    /**
     * 将RPCFuture保存到线程的上下文
     * @param rpcFuture
     */
    public void setRPCFuture(RPCFuture rpcFuture){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * 获取RPCFuture
     */
    public RPCFuture getRPCFutre(){
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    /**
     * 移除RPCFuture
     */
    public void removeRPCFutre(){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
