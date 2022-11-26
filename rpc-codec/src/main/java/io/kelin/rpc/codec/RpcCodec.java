package io.kelin.rpc.codec;

import io.kelin.rpc.serialization.api.Serialization;
import io.kelin.rpc.serialization.jdk.JdkSerialization;

/**
 * 实现编解码的接口，提供序列化和反序列化的默认方法
 */
public interface RpcCodec {

    default Serialization getJdkSerialization(){
        return new JdkSerialization();
    }
}
