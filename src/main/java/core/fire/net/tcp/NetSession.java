/**
 * 
 */
package core.fire.net.tcp;

import java.net.SocketAddress;

/**
 * 网络连接会话
 * 
 * @author lhl
 *
 *         2016年1月29日 下午5:15:01
 */
public interface NetSession
{
    /**
     * @return 返回会话唯一id
     */
    long getId();

    /**
     * 往对端发送消息
     * 
     * @param message
     */
    void send(Object message);

    /**
     * 关闭连接
     */
    void close();

    /**
     * @return 返回true表示目前已经建立连接
     */
    boolean isConnected();

    /**
     * @return 返回对端地址
     */
    SocketAddress getRemoteAddress();

    /**
     * 
     * @param key
     * @param val
     */
    void setAttachment(String key, Object val);

    /**
     * @param key
     * @return
     */
    Object getAttachment(String key);
}
