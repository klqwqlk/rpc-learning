package io.kelin.rpc.consumer.common;

import io.kelin.rpc.common.threadpool.ClientThreadPool;
import io.kelin.rpc.consumer.common.handler.RpcConsumerHandler;
import io.kelin.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.proxy.api.consumer.Consumer;
import io.kelin.rpc.proxy.api.future.RPCFuture;
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
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception {
        //TODO 暂时写死，后续引入注册中心，从注册中心获取
        String serviceAddress = "127.0.0.1";
        int port = 27880;
        String key = serviceAddress.concat("_").concat(String.valueOf(port));
        RpcConsumerHandler handler = handlerMap.get(key);
        //缓存中无RpcClientHandler
        if(handler == null){
            handler = getRpcConsumerHandler(serviceAddress,port);
            handlerMap.put(key,handler);
        }else if(!handler.getChannel().isActive()){ //不活跃
            handler.close();
            handler = getRpcConsumerHandler(serviceAddress,port);;
            handlerMap.put(key,handler);
        }
        RpcRequest request = protocol.getBody();
        //根据request的oneway，async选择是同步、异步还是单向请求
        return handler.sendRequest(protocol,request.getAsync(),request.getOneway());

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
