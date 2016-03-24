/**
 * 
 */
package core.fire.executor;

import core.fire.net.tcp.Packet;
import io.netty.channel.Channel;

/**
 * 逻辑处理器
 * <p>
 * 请勿直接实现该接口，继承{@linkplain AbstractHandler}也许是个更好的选择
 * 
 * @author lhl
 *
 *         2016年1月30日 下午3:12:02
 */
public interface Handler
{
    /**
     * @param channel 网络会话
     * @param packet 请求数据
     */
    void handle(Channel channel, Packet packet);
}
