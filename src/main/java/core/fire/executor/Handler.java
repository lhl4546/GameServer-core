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
     */
    void handle(SocketRequest request);
}
