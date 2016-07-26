/**
 * 
 */
package core.fire.net.tcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessage;

/**
 * TCP协议处理器注解，TCP协议指的是实现了{@link core.fire.net.tcp.Handler}的子类，给这种子类加上
 * {@code RequestHandler}注解以便{@code TcpDispatcher}能够扫描并注册协议处理器
 * 
 * @author lhl
 *
 *         2016年2月18日 下午1:59:22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TcpRequestHandler {
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
