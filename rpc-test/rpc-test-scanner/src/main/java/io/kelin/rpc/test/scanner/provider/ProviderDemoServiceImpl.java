package io.kelin.rpc.test.scanner.provider;

import io.kelin.rpc.annotation.RpcService;
import io.kelin.rpc.test.scanner.service.DemoService;

/**
 * @version 1.0.0
 * @description DemoServie实现类
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "io.kelin.rpc.test.scanner.service.DemoService",version = "1.0.0",group = "kelin")
public class ProviderDemoServiceImpl implements DemoService {

}
