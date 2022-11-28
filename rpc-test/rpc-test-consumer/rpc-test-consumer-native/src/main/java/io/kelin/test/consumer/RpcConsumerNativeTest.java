package io.kelin.test.consumer;

import io.kelin.rpc.consumer.RpcClient;
import io.kelin.rpc.proxy.api.async.IAsyncObjectProxy;
import io.kelin.rpc.proxy.api.future.RPCFuture;
import io.kelin.rpc.test.api.DemoService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.0.0
 * @description 测试Java原生启动服务消费者
 */
public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    public static void main(String[] args){
        RpcClient rpcClient = new RpcClient("1.0.0", "kelin", "jdk", 3000, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("kelin");
        LOGGER.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
    }

    @Test
    public void testInterfaceRpc(){
        RpcClient rpcClient = new RpcClient("1.0.0", "kelin", "jdk", 3000, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("kelin testInterfaceRpc");
        LOGGER.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        RpcClient rpcClient = new RpcClient("1.0.0", "kelin", "jdk", 3000, false, false);
        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "kelin testAsyncInterfaceRpc");
        LOGGER.info("返回的结果数据===>>> " + future.get());
        rpcClient.shutdown();
    }

}
