package io.kelin.rpc.test.scanner;

import io.kelin.rpc.common.ClassScanner;
import io.kelin.rpc.scanner.reference.RpcReferenceScanner;
import io.kelin.rpc.scanner.server.RpcServiceScanner;
import org.junit.Test;

import java.util.List;

/**
 * @version 1.0.0
 * @description 扫描测试
 */
public class ScannerTest {

    /**
     * 扫描io.kelin.rpc.test.scanner包下的所有类
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("io.kelin.rpc.test.scanner");
        classNameList.forEach(System.out::println);
    }

    /**
     * 扫描io.binghe.rpc.test.scanner包下所有标注了@RpcService注解的类
     */
    @Test
    public void testScannerClassNameListByRpcServcie() throws Exception {
        RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryServie("io.kelin.rpc.test.scanner");
    }
    /**
     * 扫描io.binghe.rpc.test.scanner包下所有标注了@RpcReference注解的类
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("io.kelin.rpc.test.scanner");
    }
}
