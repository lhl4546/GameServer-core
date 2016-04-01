/**
 * 
 */
package core.fire.net.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP请求处理器注解，使用该注解标识的类将被自动注册为HTTP请求处理器
 * 
 * @author lhl
 *
 *         2016年3月28日 下午4:45:32
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpRequestHandler {

    /**
     * 必须以"/"开头
     * 
     * @return
     */
    String path();

    /**
     * false表示该处理器处于停用状态，默认为true
     * 
     * @return
     */
    boolean isEnabled() default true;
}
