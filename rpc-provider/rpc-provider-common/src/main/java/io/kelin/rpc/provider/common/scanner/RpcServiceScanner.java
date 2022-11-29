package io.kelin.rpc.provider.common.scanner;

import io.kelin.rpc.annotation.RpcService;
import io.kelin.rpc.common.ClassScanner;
import io.kelin.rpc.common.helper.RpcServiceHelper;
import io.kelin.rpc.protocol.meta.ServiceMeta;
import io.kelin.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @version  1.0.0
 * @description @RpcService注解扫描器
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解的类
     */
    public static Map<String,Object> doScannerWithRpcServiceAnnotationFilterAndRegistryServie(String host, int port, String scanPackage , RegistryService registryService) throws Exception {
        Map<String,Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if(classNameList ==null || classNameList.isEmpty()){
            return handlerMap;
        }

        classNameList.stream().forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null){
                    //优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcService),rpcService.version(),rpcService.group(),host,port);
                    //将元数据注册到注册中心
                    registryService.register(serviceMeta);
                     //handlerMap中的key=服务名称#服务版本#服务分组
                     String serviceName = getServiceName(rpcService);
                     String key = RpcServiceHelper.buildServiceKey(serviceName, rpcService.version(), rpcService.group());
                     handlerMap.put(key,clazz.newInstance());

                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });

        return handlerMap;
    }

    /**
     * 获取serviceName
     */
    private static String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

}