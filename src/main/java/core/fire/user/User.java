/**
 * 
 */
package core.fire.user;

import static core.fire.net.tcp.TcpDispatcher.SEQUENCE_KEY;

import core.fire.executor.Sequence;
import core.fire.net.tcp.IPacket;
import io.netty.channel.Channel;

/**
 * 抽象用户对象，实现基本的任务队列功能
 * 
 * @author lihuoliang
 *
 */
public class User
{
    private Sequence sequence;
    private Channel channel;

    protected User(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * 添加用户任务，该任务将在用户线程顺序执行
     * 
     * @param task
     */
    public void addTask(Runnable task) {
        sequence.addTask(task);
    }

    /**
     * 设置玩家所关联的网络连接对象
     * 
     * @param channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
        this.channel.attr(SEQUENCE_KEY).set(sequence);
    }

    /**
     * 向玩家发送消息
     * 
     * @param packet
     */
    public void send(IPacket packet) {
        channel.writeAndFlush(packet);
    }
}
