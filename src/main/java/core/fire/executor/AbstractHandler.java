/**
 * 
 */
package core.fire.executor;

import com.google.protobuf.GeneratedMessage;

import core.fire.AppStart;
import core.fire.net.tcp.Packet;

/**
 * {@code T}为具体请求类型
 * 
 * @author lhl
 *
 *         2016年2月26日 下午4:14:31
 */
public abstract class AbstractHandler<T extends GeneratedMessage> implements Handler
{
    private static final DispatcherHandler dispatcher;

    static {
        dispatcher = AppStart.INSTANCE.appCtx.getBean(DispatcherHandler.class);
    }

    /**
     * 将字节数组参数转换为PB类型参数
     * 
     * @param packet
     * @return 若字节数组为空则返回null
     */
    @SuppressWarnings("unchecked")
    protected T parseParam(Packet packet) {
        if (packet.body != null) {
            GeneratedMessage paramType = dispatcher.getParamType(packet.code);
            return (T) packet.toProto(paramType);
        }
        return null;
    }

    // ADVICE
    //
    // @Override
    // public final void handle(Channel channel, Packet packet) {
    // T message = parseParam(packet);
    // handle(channel, message);
    // }
    //
    // /**
    // * @param channel
    // * @param message 当请求{@code Packet}的包体{@code body}为空时该参数为空
    // */
    // protected abstract void handle(Channel channel, T message);
}
