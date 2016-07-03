/**
 * 
 */
package core.fire.rpc.json.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC请求注解
 * 
 * @author lihuoliang
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcRequestHandler {
    /**
     * @return RPC请求方法名
     */
    String methodName();
}
