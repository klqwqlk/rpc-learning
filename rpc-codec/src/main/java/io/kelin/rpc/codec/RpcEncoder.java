package io.kelin.rpc.codec;

import io.kelin.rpc.common.utils.SerializationUtils;
import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 实现RPC编码
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements  RpcCodec{
    /**
     *   编码消息头和消息体
     * @param ctx
     * @param protocol
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> protocol, ByteBuf byteBuf) throws Exception {
        RpcHeader header = protocol.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        //得到序列化类型
        String serializationType = header.getSerializationType();
        //TODO Serializaiton是扩展点
        Serialization serialization = getJdkSerialization();
        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes("UTF-8"));
        byte[] data = serialization.serialize(protocol.getBody());
        //消息体长度
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

    }
}
