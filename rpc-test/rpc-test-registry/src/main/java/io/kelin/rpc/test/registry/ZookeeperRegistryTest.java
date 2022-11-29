package io.kelin.rpc.test.registry;


import io.kelin.rpc.protocol.meta.ServiceMeta;
import io.kelin.rpc.registry.api.RegistryService;
import io.kelin.rpc.registry.api.config.RegistryConfig;
import io.kelin.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @version 1.0.0
 * @description 注册测试
 */

public class ZookeeperRegistryTest {
    private RegistryService registryService;

    private ServiceMeta serviceMeta;

    @Before
    public void init() throws Exception{
        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper");
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest.class.getName(), "1.0.0", "kelin", "127.0.0.1", 8080);
    }

    @Test
    public void testRegister() throws Exception {
        this.registryService.register(serviceMeta);
    }

    @Test
    public void testUnRegister() throws Exception {
        this.registryService.unRegister(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception {
        this.registryService.discovery(RegistryService.class.getName(), "kelin".hashCode());
    }

    @Test
    public void testDestroy() throws IOException {
        this.registryService.destroy();
    }

}
