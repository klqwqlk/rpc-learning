package io.kelin.rpc.protocol.header;

import io.kelin.rpc.common.id.IdFactory;
import io.kelin.rpc.constants.RpcConstants;
import io.kelin.rpc.protocol.enumeration.RpcType;

/**
 * @version 1.0.0
 * @description RpcHeaderFactory
 */

public class RpcHeaderFactory {
    public static RpcHeader getRequestHeader(String serializationType){
        RpcHeader header = new RpcHeader();
        long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(requestId);
        header.setMsgType((byte) RpcType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        header.setSerializationType(serializationType);
        return header;

    }
}
