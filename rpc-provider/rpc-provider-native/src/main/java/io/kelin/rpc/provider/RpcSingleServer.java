package io.kelin.rpc.provider;

import io.kelin.rpc.provider.common.server.base.BaseServer;
import io.kelin.rpc.common.scanner.server.RpcServiceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.0.0
 * @description 以java原生方式启动Rpc
 */
public class RpcSingleServer extends BaseServer {

    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress,String scanPackage, String reflectType) {
        //调用父类构造方法
        super(serverAddress,reflectType);

        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryServie(scanPackage);
        } catch (Exception e) {
            logger.error("RPC Server init error ", e);
        }
    }
}
