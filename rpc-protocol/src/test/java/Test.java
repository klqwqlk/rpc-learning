import io.kelin.rpc.protocol.RpcProtocol;
import io.kelin.rpc.protocol.header.RpcHeader;
import io.kelin.rpc.protocol.header.RpcHeaderFactory;
import io.kelin.rpc.protocol.request.RpcRequest;

/**
 * @author binghe(公众号 ： 冰河技术)
 * @version 1.0.0
 * @description
 */
public class Test {
    public static RpcProtocol<RpcRequest> getRpcProtocol(){
        RpcHeader header = RpcHeaderFactory.getRequestHeader("jdk");
        RpcRequest body = new RpcRequest();
        body.setOneway(false);
        body.setAsync(false);
        body.setClassName("io.kelin.rpc.demo.RpcProtocol");
        body.setMethodName("hello");
        body.setGroup("binghe");
        body.setParameters(new Object[]{"binghe"});
        body.setParameterTypes(new Class[]{String.class});
        body.setVersion("1.0.0");
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setBody(body);
        protocol.setHeader(header);
        return protocol;
    }
}

