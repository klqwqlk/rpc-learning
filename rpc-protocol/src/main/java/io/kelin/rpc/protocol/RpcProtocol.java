package io.kelin.rpc.protocol;

import io.kelin.rpc.protocol.header.RpcHeader;

import java.io.Serializable;

/**
 * @version 1.0.0
 * @description rpc协议
 * @param <T>
 */
public class RpcProtocol<T> implements Serializable {
    private static final long serialVersionUID = 292789485166173277L;

    /**
     * 消息头
     */
    private RpcHeader header;
    /**
     * 消息体
     */
    private T body;

    public RpcHeader getHeader() {
        return header;
    }

    public void setHeader(RpcHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
