package io.kelin.rpc.test.consumer.handler;

import io.kelin.rpc.consumer.common.RpcConsumer;
import io.kelin.rpc.consumer.common.callback.AsyncRPCCallback;
import io.kelin.rpc.consumer.common.context.RpcContext;
import io.kelin.rpc.consumer.common.future.RPCFuture;
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

    public static void main(String[] args) throws Exception {

        RpcConsumer consumer = RpcConsumer.getInstance();
        RPCFuture rpcFuture = consumer.sendRequest(getRpcRequestProtocol());
        rpcFuture.addCallback(new AsyncRPCCallback() {
            @Override
            public void onSuccess(Object result) {
                LOGGER.info("callback 从服务消费者获取到的数据===>>>" + result);
            }

            @Override
            public void onException(Exception e) {
                LOGGER.info("callback 抛出了异常===>>>" + e);
            }
        });
        Thread.sleep(200);
        consumer.close();


        //同步调用
//        RpcConsumer consumer = RpcConsumer.getInstance();
//        //阻塞到获取到result
//        Object result = consumer.sendRequest(getRpcRequestProtocol());
//        System.out.println("do something...");
//        System.out.println("done...");
//        LOGGER.info("从服务消费者接收到的数据===>>> "+ result.toString());
//        consumer.close();

        //异步调用
//        RpcConsumer consumer = RpcConsumer.getInstance();
//        consumer.sendRequest(getRpcRequestProtocol());
//        //可以先完成工作，晚些再获取结果（get())
//        System.out.println("do something...");
//        System.out.println("done...");
//        RPCFuture future = RpcContext.getContext().getRPCFutre();
//        LOGGER.info("从服务消费者接收到的数据===>>> "+ future.get());
//        consumer.close();

        //单向调用
//        RpcConsumer consumer = RpcConsumer.getInstance();
//        consumer.sendRequest(getRpcRequestProtocol());
//        LOGGER.info("无需获取返回的响应数据");
//        consumer.close();
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
        request.setAsync(false); //同步、异步？
        request.setOneway(false);   //单向？
        protocol.setBody(request);
        return protocol;
    }
}
