package io.kelin.rpc.consumer.common.callback;

/**
 * @version 1.0.0
 * @description 异步回调接口
 */
public interface AsyncRPCCallback {
    /**
     * 成功后的回调方法
     */
    void onSuccess(Object result);

    /**
     * 异常的回调方法
     */
    void onException(Exception e);
}

