package io.kelin.rpc.provider.common.server.base;

import io.kelin.rpc.codec.RpcDecoder;
import io.kelin.rpc.codec.RpcEncoder;
import io.kelin.rpc.provider.common.handler.RpcProviderHandler;
import io.kelin.rpc.provider.common.server.api.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @version  1.0.0
 * @description 基础服务
 */
public class BaseServer implements Server {

    private final Logger loger = LoggerFactory.getLogger(BaseServer.class);
    //主机域名或者IP地址
    protected String host = "127.0.0.1";
    //端口号
    protected  int port = 27110;
    //存储的实体类关系
    protected Map<String,Object> handlerMap = new HashMap<>();

    public BaseServer(String serverAddress){
        if(!StringUtils.isEmpty(serverAddress)){
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
    }

    @Override
    public void startNettyServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //TODO 预留编解码，需要实现自定义协议
//                                .addLast(new StringDecoder())
//                                .addLast(new StringEncoder())
                                //采用自定义协议
                                .addLast(new RpcDecoder())
                                .addLast(new RpcEncoder())
                                .addLast(new RpcProviderHandler(handlerMap));
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host,port).sync();
            loger.info("Server started on {} : {}",host,port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
           loger.error("PRC Server start error", e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
