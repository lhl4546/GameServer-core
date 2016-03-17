/**
 * 
 */
package core.fire.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.net.NetSession;
import core.fire.net.tcp.Packet;

/**
 * 可执行任务，配合消息队列{@link Sequence}使用可以达到消息队列自动执行的目的
 * 
 * @author lhl
 *
 */
public class RunnableTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(RunnableTask.class);
    private Handler handler; // 消息处理器
    private NetSession session; // 消息队列
    private Packet packet; // 消息包
    private Sequence sequence; // 消息队列

    /**
     * @param handler 处理器
     * @param session 网络会话
     * @param packet 消息包
     * @param sequence 任务队列
     */
    public RunnableTask(Handler handler, NetSession session, Packet packet, Sequence sequence) {
        this.handler = handler;
        this.session = session;
        this.packet = packet;
        this.sequence = sequence;
    }

    @Override
    public void run() {
        try {
            handler.handle(session, packet);
        } catch (Throwable t) {
            LOG.error("{}", toString(), t);
        } finally {
            sequence.dequeue();
        }
    }

    @Override
    public String toString() {
        return "RunnableTask: [handler=" + handler.getClass() + ", session=" + session + ", pkt=" + packet + ", data="
                + packet + "]";
    }
}
