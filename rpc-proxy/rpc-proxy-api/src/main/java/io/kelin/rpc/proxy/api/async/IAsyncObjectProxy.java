package io.kelin.rpc.proxy.api.async;

import io.kelin.rpc.proxy.api.future.RPCFuture;

/**
 * @version 1.0.0
 * @description 异步访问接口
 */
public interface IAsyncObjectProxy {

    /**
     * 异步代理对象调用方法
     * @param funcName 方法名称
     * @param args 方法参数
     * @return 封装好的RPCFuture对象
     */
    RPCFuture call(String funcName, Object... args);
}

