/**
 * 
 */
package core.fire.executor;

import core.fire.net.NetSession;
import core.fire.net.tcp.Packet;

/**
 * 逻辑处理器
 * 
 * @author lhl
 *
 *         2016年1月30日 下午3:12:02
 */
public interface Handler
{
    void handle(NetSession session, Packet packet);
}
