package io.kelin.rpc.consumer.common;

import io.kelin.rpc.common.helper.RpcServiceHelper;
import io.kelin.rpc.common.threadpool.ClientThreadPool;
import io.kelin.rpc.consumer.common.handler.RpcConsumerHandler;
import io.kelin.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.kelin.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.meta.ServiceMeta;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.proxy.api.consumer.Consumer;
import io.kelin.rpc.proxy.api.future.RPCFuture;
import io.kelin.rpc.proxy.api.object.ObjectProxy;
import io.kelin.rpc.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0.0
 * @description 服务消费者
 */
public class RpcConsumer implements Consumer {
    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;

    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private  RpcConsumer(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
    }

    public static RpcConsumer getInstance(){
        if(instance == null){
            synchronized (RpcConsumer.class){
                if(instance == null){
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    public void close(){
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {

        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] params = request.getParameters();
        int invokerHashCode = (params == null || params.length <= 0)?
                serviceKey.hashCode():params[0].hashCode();
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode);
        if(serviceMeta != null){
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            //缓存中无RpcClientHandler
            if(handler == null){
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(),serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta,handler);
            }else if(!handler.getChannel().isActive()){ //不活跃
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(),serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta,handler);
            }
            //根据request的oneway，async选择是同步、异步还是单向请求
            return handler.sendRequest(protocol,request.getAsync(),request.getOneway());
        }

        return null;
    }

    /**
     * 创建连接并返回RpcClientHandler
     */
    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress,port).sync();
        channelFuture.addListener((ChannelFutureListener)listener ->{
            if(channelFuture.isSuccess()){
                logger.info("connect rpc server {} on port {} success.", serviceAddress,port);
            }else{
                logger.error("connect rpc server {} on port {} failed.",serviceAddress,port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
