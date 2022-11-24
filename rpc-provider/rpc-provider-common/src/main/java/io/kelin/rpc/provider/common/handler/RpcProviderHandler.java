package io.kelin.rpc.provider.common.handler;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.enumeration.RpcType;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @version 1.0.0
 * @description PRC服务提供者的Handler处理类
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    private final Map<String, Object> handlerMap;

    public  RpcProviderHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> protocol) throws Exception {
        logger.info("RPC服务提供者收到的数据为====>>> " + JSONObject.toJSONString(protocol));
        logger.info("handlerMap中存放的数据如下所示: ");
        for(Map.Entry<String,Object> entry : handlerMap.entrySet()){
            logger.info(entry.getKey() + " === " + entry.getValue());
        }
        //取出消息头和消息体(请求信息)
        RpcHeader header = protocol.getHeader();
        RpcRequest request = protocol.getBody();
        //构建响应协议数据
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        //header中的消息类型设置为响应类型
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcResponse response = new RpcResponse();
        response.setResult("数据交互成功");
        response.setAsync(request.getAsync());
        response.setOneway(request.getOneway());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        //返回RpcPotocol<RpcResponse> （响应信息）
        channelHandlerContext.writeAndFlush(responseRpcProtocol);
    }
}
