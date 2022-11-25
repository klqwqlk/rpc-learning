package io.kelin.rpc.test.provider.service.impl;

import io.kelin.rpc.annotation.RpcService;
import io.kelin.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version 1.0.0
 * @description DemoService实现类
 */

@RpcService(interfaceClass = DemoService.class,interfaceClassName = "io.kelin.rpc.test.api.DemoService",version = "1.0.0",group = "kelin")
public class ProviderDemoServiceImpl implements DemoService {

    private final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("调用hello方法传入的参数为===>>>{}",name);
        return "hello " + name;
    }
}
