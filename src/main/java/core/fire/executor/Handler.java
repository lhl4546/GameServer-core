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
     * 处理协议，协议应答由子类自行发送
     * 
     * @param request
     * @param response
     */
    void handle(SocketRequest request, SocketResponse response);
}
