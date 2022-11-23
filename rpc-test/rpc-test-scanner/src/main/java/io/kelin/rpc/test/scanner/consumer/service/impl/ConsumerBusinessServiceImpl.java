package io.kelin.rpc.test.scanner.consumer.service.impl;

import io.kelin.rpc.annotation.RpcReference;
import io.kelin.rpc.test.scanner.consumer.service.ConsumerBusinessService;
import io.kelin.rpc.test.scanner.service.DemoService;

/**
 * @version  1.0.0
 * @description 服务消费者业务逻辑实现类
 */
public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {

    @RpcReference(registerType = "zookeeper", registryAddress = "127.0.0.1:2181", version = "1.0.0", group = "kelin")
    private DemoService demoService;
}
