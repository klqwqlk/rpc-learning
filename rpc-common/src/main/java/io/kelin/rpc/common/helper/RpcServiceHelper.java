package io.kelin.rpc.common.helper;

/**
 * @version 1.0.0
 * @description RPC服务帮助类
 */
public class RpcServiceHelper {

    /**
     *  拼接字符串
     * @param serviceName 服务名称
     * @param serviceVersion 服务版本号
     * @param serviceGroup  服务分组
     * @return   服务名称#服务版本号#服务分组
     */
    public static String buildServiceKey(String serviceName, String serviceVersion,String serviceGroup) {
        return String.join("#", serviceName,serviceVersion,serviceGroup);
    }
}
