package io.kelin.rpc.proxy.jdk;


import io.kelin.rpc.proxy.api.BaseProxyFactory;
import io.kelin.rpc.proxy.api.ProxyFactory;


import java.lang.reflect.Proxy;

/**
 * @version 1.0.0
 * @description JDK动态代理
 */

public class JdkProxyFactory <T> extends BaseProxyFactory<T> implements ProxyFactory {

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }

}
