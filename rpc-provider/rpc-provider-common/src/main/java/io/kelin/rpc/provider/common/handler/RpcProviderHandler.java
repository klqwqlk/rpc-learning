package io.kelin.rpc.provider.common.handler;


import io.kelin.rpc.common.helper.RpcServiceHelper;
import io.kelin.rpc.common.threadpool.ServerThreadPool;
import io.kelin.rpc.constants.RpcConstants;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.enumeration.RpcStatus;
import io.kelin.rpc.protocol.enumeration.RpcType;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @version 1.0.0
 * @description PRC服务提供者的Handler处理类
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    //存储 服务名称#服务版本#服务分组 与 对象实例的映射关系
    private final Map<String, Object> handlerMap;

    //采用哪种类型调用真实方法
    private final String reflectType;

    public  RpcProviderHandler(String reflectType, Map<String,Object> handlerMap){
        this.reflectType = reflectType;
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> protocol) throws Exception {

        //向线程池提交任务
        ServerThreadPool.submit(()->{
            //取出消息头和消息体(请求信息)
            RpcHeader header = protocol.getHeader();
            //header中的消息类型设置为响应类型
            header.setMsgType((byte) RpcType.RESPONSE.getType());
            RpcRequest request = protocol.getBody();
            logger.debug("Receive Request "+header.getRequestId());
            //构建响应协议数据
            RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
            RpcResponse response = new RpcResponse();

             //处理请求
            Object result = null;
            try {
                result = handle(request);
                response.setResult(result);
                response.setAsync(request.getAsync());
                response.setOneway(request.getOneway());
                header.setStatus((byte) RpcStatus.SUCCESS.getCode());
            } catch (Throwable throwable) {
                response.setError(throwable.toString());
                header.setStatus((byte) RpcStatus.FAIL.getCode());
                logger.error("RPC Server handle request error", throwable);
            }
            //封装响应
            responseRpcProtocol.setHeader(header);
            responseRpcProtocol.setBody(response);
            //返回RpcPotocol<RpcResponse> （响应信息）
            channelHandlerContext.writeAndFlush(responseRpcProtocol)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            logger.debug("Send response for request " + header.getRequestId());
                        }
                    });
        });


    }

    private Object handle(RpcRequest request) throws Throwable{
        //请求的相关服务
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(),request.getGroup());
        //获取对应的服务对象
        Object serviceBean = handlerMap.get(serviceKey);
        if(serviceBean == null){
            throw new RuntimeException(String.format("service not exist : %s %s ", request.getClassName(),request.getMethodName()));
        }

        //获取请求的方法，参数...
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);

        if(parameterTypes != null && parameterTypes.length > 0){
            for(int i=0;i<parameterTypes.length;i++){
                logger.debug(parameterTypes[i].getName());
            }
        }

        if(parameters != null && parameters.length > 0){
            for(int i=0;i<parameters.length;i++){
                logger.debug(parameters[i].toString());
            }
        }

        //反射调用服务提供者的方法
        return invokeMethod(serviceBean,serviceClass,methodName,parameterTypes,parameters);
    }

    //TODO 目前使用JDK动态代理方式，此处埋点
    private Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        switch (this.reflectType){
            case RpcConstants.REFLECT_TYPE_JDK:
                return this.invokeJDKMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            case RpcConstants.REFLECT_TYPE_CGLIB:
                return this.invokeCGLIBMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            default:
                throw new IllegalArgumentException("not support reflect type");
        }
    }

    //jdk reflect
    private Object invokeJDKMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
            logger.info("use jdk reflect type invoke method...");
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        }

        //cglib reflect
    private Object invokeCGLIBMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use cglib reflect type invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName,parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception ", cause);
        ctx.close();
    }
}
