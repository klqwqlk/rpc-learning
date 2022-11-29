package io.kelin.rpc.provider;

import io.kelin.rpc.provider.common.scanner.RpcServiceScanner;
import io.kelin.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.0.0
 * @description 以java原生方式启动Rpc
 */
public class RpcSingleServer extends BaseServer {

    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress, String registryAddress, String registryType,String scanPackage, String reflectType) {
        //调用父类构造方法
        super(serverAddress,registryAddress,reflectType,reflectType);

        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryServie(this.host,this.port,scanPackage,registryService);
        } catch (Exception e) {
            logger.error("RPC Server init error ", e);
        }
    }
}
