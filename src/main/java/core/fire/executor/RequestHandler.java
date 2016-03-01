/**
 * 
 */
package core.fire.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessage;

/**
 * @author lhl
 *
 *         2016年2月18日 下午1:59:22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestHandler {
    /**
     * 请求指令
     * 
     * @return
     */
    short code();

    /**
     * 请求参数类型
     * 
     * @return
     */
    Class<? extends GeneratedMessage> requestParamType();
}
