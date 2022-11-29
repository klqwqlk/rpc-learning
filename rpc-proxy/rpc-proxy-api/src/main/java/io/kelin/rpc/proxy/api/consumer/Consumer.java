package io.kelin.rpc.proxy.api.consumer;

import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.proxy.api.future.RPCFuture;
import io.kelin.rpc.registry.api.RegistryService;

/**
 * @version 1.0.0
 * @description 服务消费者
 */
public interface Consumer {

    /**
     * 消费者发送 request 请求
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception;
}
