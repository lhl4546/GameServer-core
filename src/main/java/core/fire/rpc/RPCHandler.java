/**
 * 
 */
package core.fire.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.thrift.TProcessor;

/**
 * RPC处理器注解
 * 
 * @author lhl
 *
 *         2016年2月22日 上午9:33:01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RPCHandler {
    /**
     * @return RPC service name
     */
    String serviceName();

    /**
     * @return RPC handler super interface
     */
    Class<?> iface();

    /**
     * @return RPC processor
     */
    Class<? extends TProcessor> processor();
}
