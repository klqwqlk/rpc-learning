package io.kelin.rpc.serialization.jdk;

import io.kelin.rpc.common.exception.SerializerException;
import io.kelin.rpc.serialization.api.Serialization;

import java.io.*;

/**
 * Jdk Serialization
 */
public class JdkSerialization implements Serialization {
    /**
     * 序列化
     * @param obj 序列化对象
     * @param <T>
     * @return  序列化字节流
     */
    @Override
    public <T> byte[] serialize(T obj) {
        if(obj == null){
            throw new SerializerException("serialize object is null");
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeObject(obj);
            return os.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    /**
     *  反序列化
     * @param data 需要反序列化的字节流
     * @param cls  对象类信息
     * @param <T>
     * @return  反序列化对象
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if(data == null){
            throw new SerializerException("deserialize data is null");
        }
        try{
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(is);
            return (T) in.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
