package io.kelin.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.kelin.rpc.consumer.common.context.RpcContext;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.kelin.rpc.proxy.api.future.RPCFuture;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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

    //存放请求id与RPCFuture的映射关系
    private Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>();

    public Channel getChannel(){
        return channel;
    }

    public SocketAddress getRemotePeer(){
        return remotePeer;
    }


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


        RPCFuture rpcFuture = pendingRPC.remove(requestId);
        if(rpcFuture != null){
//            Thread.sleep(5000);
            rpcFuture.done(rpcResponseRpcProtocol);
        }

    }

    /**
     * 服务消费者向服务提供者发送请求
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, boolean async, boolean oneway) throws Exception {
        logger.info("服务消费者发送的数据===>>> "+ JSONObject.toJSONString(protocol));
        return oneway? this.sendRequestOneway(protocol):async? sendRequestAsync(protocol):sendRequestSync(protocol);
    }

    /**
     *  同步调用发送请求
     * @param protocol
     * @return
     */
    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = getRPCFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    /**
     * 异步调用发送
     */
    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = getRPCFuture(protocol);
        //如果是异步调用，则将RPCFuture放入RpcContext
        RpcContext.getContext().setRPCFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }
    /**
     * 单向请求
     */
    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol){
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 根据传入的RpcRequest类型的协议对象，构建RPCFuture对象，并将其添加到pendingRPC中。
     * @param protocol Rpc请求协议
     * @return
     */
    public RPCFuture getRPCFuture(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = new RPCFuture(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRPC.put(requestId,rpcFuture);
        return rpcFuture;
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
