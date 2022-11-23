package io.kelin.rpc.scanner.server;

import io.kelin.rpc.annotation.RpcService;
import io.kelin.rpc.common.ClassScanner;
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
    public static Map<String,Object> doScannerWithRpcServiceAnnotationFilterAndRegistryServie(/*String host,int port,*/ String scanPackage /*,RegistryService registryService*/) throws Exception {
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
//                    TODO 后续逻辑向注册中心注册服务元数据，同时向handlerMap中记录标注了RpcService注解的类实例

                     //handlerMap中的key先简单存储为serviceName+version+group,后根据实际情况处理key
                    String serviceName = getServiceName(rpcService);
                    String key = serviceName.concat(rpcService.version()).concat(rpcService.group());
                    handlerMap.put(key,clazz.newInstance());
//                    LOGGER.info("当前标注了@RpcService注解的类实例名称===>>> " + clazz.getName());
//                    LOGGER.info("@RpcService注解上标注的属性信息如下：");
//                    LOGGER.info("interfaceClass===>>> " + rpcService.interfaceClass().getName());
//                    LOGGER.info("interfaceClassName===>>> " + rpcService.interfaceClassName());
//                    LOGGER.info("version===>>> " + rpcService.version());
//                    LOGGER.info("group===>>> " + rpcService.group());
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
