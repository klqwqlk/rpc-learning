package io.kelin.rpc.test.consumer.codec.init;

import io.kelin.rpc.codec.RpcDecoder;
import io.kelin.rpc.codec.RpcEncoder;
import io.kelin.rpc.test.consumer.codec.handler.RpcTestConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcDecoder());
        cp.addLast(new RpcEncoder());
        cp.addLast(new RpcTestConsumerHandler());
    }
}
