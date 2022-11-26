package io.kelin.rpc.test.consumer.handler;

import io.kelin.rpc.consumer.common.RpcConsumer;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.header.RpcHeaderFactory;
import io.kelin.rpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version  1.0.0
 * @description 测试服务消费者
 */
public class RpcConsumerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);

    public static void main(String[] args) throws InterruptedException {
        RpcConsumer consumer = RpcConsumer.getInstance();
        Object result = consumer.sendRequest(getRpcRequestProtocol());
        LOGGER.info("从服务消费者接收到的数据===>>> "+ result.toString());
        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol(){
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.kelin.rpc.test.api.DemoService");
        request.setGroup("kelin");
        request.setMethodName("hello");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"kelin"});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }
}
