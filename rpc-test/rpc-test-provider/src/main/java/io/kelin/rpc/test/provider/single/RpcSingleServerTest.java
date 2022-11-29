package io.kelin.rpc.test.provider.single;

import io.kelin.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @version 1.0.0
 * @description 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "127.0.0.1:2181","zookeeper","io.kelin.rpc.test","cglib");
        singleServer.startNettyServer();
    }
}
