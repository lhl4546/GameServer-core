/**
 * 
 */
package core.fire.executor;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.GeneratedMessage;

import core.fire.net.NetSession;
import core.fire.net.netty.Packet;

/**
 * {@code T}为具体请求类型
 * 
 * @author lhl
 *
 *         2016年2月26日 下午4:14:31
 */
public abstract class AbstractHandler<T extends GeneratedMessage> implements Handler
{
    @Autowired
    private DispatcherHandler dispatcher;

    @Override
    public final void handle(NetSession session, Packet packet) {
        T message = parseParam(packet);
        handle(session, message);
    }

    /**
     * 将字节数组参数转换为PB类型参数
     * 
     * @param packet
     * @return 若字节数组为空则返回null
     */
    @SuppressWarnings("unchecked")
    private T parseParam(Packet packet) {
        if (packet.body != null) {
            GeneratedMessage paramType = dispatcher.getParamType(packet.code);
            return (T) packet.toProto(paramType);
        }
        return null;
    }

    /**
     * @param session
     * @param message 当请求{@code Packet}的包体{@code body}为空时该参数为空
     */
    protected abstract void handle(NetSession session, T message);
}
