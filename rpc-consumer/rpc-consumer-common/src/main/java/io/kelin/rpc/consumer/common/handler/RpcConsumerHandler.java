package io.kelin.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0.0
 * @ description Rpc消费处理器
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;
    private SocketAddress remotePeer;

    public Channel getChannel(){
        return channel;
    }

    public SocketAddress getRemotePeer(){
        return remotePeer;
    }

    private Map<Long,RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> rpcResponseRpcProtocol) throws Exception {
        if(rpcResponseRpcProtocol == null){
            return;
        }
        logger.info("服务消费者接收到的数据===>>> "+ JSONObject.toJSONString(rpcResponseRpcProtocol));
        RpcHeader header = rpcResponseRpcProtocol.getHeader();
        Long requestId = header.getRequestId();
        //放入与请求匹配的响应到对应的映射中，供服务消费者同步接收数据
        pendingResponse.put(requestId,rpcResponseRpcProtocol);
    }

    /**
     * 服务消费者向服务提供者发送请求
     */
    public Object sendRequest(RpcProtocol<RpcRequest> protocol){
        logger.info("服务消费者发送的数据===>>> "+ JSONObject.toJSONString(protocol));
        channel.writeAndFlush(protocol);

        //异步转同步
        while(true){
            RpcProtocol<RpcResponse> responseProtocol = pendingResponse.remove(protocol.getHeader().getRequestId());
            if(responseProtocol != null){
               return responseProtocol.getBody().getResult();
            }
        }
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
