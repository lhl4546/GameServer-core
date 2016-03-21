/**
 * 
 */
package core.fire.net.http;

/**
 * HTTP处理器注解
 * @author lhl
 *
 *         2016年3月17日 下午4:03:00
 */
public @interface HttpRequestHandler {
    /**
     * @return 处理的uri，必须以"/"开头
     */
    String uri();
}
