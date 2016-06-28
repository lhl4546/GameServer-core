/**
 * 
 */
package core.fire.executor;

/**
 * TCP协议处理器
 * 
 * @author lhl
 *
 *         2016年1月30日 下午3:12:02
 */
public interface Handler
{
    /**
     * @param request
     * @param response
     */
    void handle(SocketRequest request, SocketResponse response);
}
