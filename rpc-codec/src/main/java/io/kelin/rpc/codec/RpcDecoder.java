package io.kelin.rpc.codec;

import io.kelin.rpc.common.utils.SerializationUtils;
import io.kelin.rpc.constants.RpcConstants;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.enumeration.RpcType;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.request.RpcRequest;
import io.kelin.rpc.protocol.response.RpcResponse;
import io.kelin.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * RPC解码操作
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {

    /**
     *    解码RPC协议
     * @param ctx
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        //验证消息长度
        if(byteBuf.readableBytes() < RpcConstants.HEADER_TOTAL_LEN){
            return;
        }

        byteBuf.markReaderIndex();

        //依次取出消息头的各个字段，以及消息体
        short magic = byteBuf.readShort();
        if(magic != RpcConstants.MAGIC){
            throw new IllegalArgumentException("magic number is illegal, "+ magic);
        }

        byte msgType = byteBuf.readByte();
        byte status = byteBuf.readByte();
        long requestId = byteBuf.readLong();

        ByteBuf serializationTypeByteBuf = byteBuf.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_COUNT);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));

        //得到消息体长度
        int dataLength = byteBuf.readInt();
        //若消息体不完整
        if( byteBuf.readableBytes() < dataLength){
            byteBuf.resetReaderIndex();
            return;
        }

        //消息体
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);


        RpcType msgTypeEnum = RpcType.findByType(msgType);
        //检查消息类型
        if(msgTypeEnum == null){
            return;
        }

        //封装消息头
        RpcHeader header = new RpcHeader();
        header.setMagic(magic);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setSerializationType(serializationType);
        header.setMsgLen(dataLength);

        //TODO Serialization是扩展点
        Serialization serialization = getJdkSerialization();
        //根据消息类型封装消息
        switch (msgTypeEnum){
            case REQUEST:
                //反序列化消息体
                RpcRequest request = serialization.deserialize(data, RpcRequest.class);
                if(request != null){
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    list.add(protocol);
                }
                break;
            case RESPONSE:
                RpcResponse response = serialization.deserialize(data, RpcResponse.class);
                if(response != null){
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    list.add(protocol);
                }
                break;
            case HEARTBEAT:
                //TODO 心跳消息
                break;
        }
    }
}
