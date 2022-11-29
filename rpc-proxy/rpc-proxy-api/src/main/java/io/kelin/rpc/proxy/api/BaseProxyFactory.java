package io.kelin.rpc.proxy.api;

import io.kelin.rpc.proxy.api.config.ProxyConfig;
import io.kelin.rpc.proxy.api.object.ObjectProxy;

/**
 * @version 1.0.0
 * @description 基础代理工厂类
 */

public abstract class BaseProxyFactory<T> implements ProxyFactory{

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getSerializationType(),
                proxyConfig.getTimeout(),
                proxyConfig.getRegistryService(),
                proxyConfig.getConsumer(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway());
    }
}
