package core.fire.executor;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import core.fire.Dumpable;
import core.fire.net.tcp.Packet;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * TCP请求，封装了一系列TCP请求相关的数据。
 * 
 * @author lhl
 *
 *         2016年6月28日 下午3:37:04
 */
public class SocketRequest implements Dumpable
{
    private Channel channel;
    private Packet packet;
    private Object requestParameter;
    private Object user;

    public SocketRequest(Channel channel, Packet packet) {
        this.channel = channel;
        this.packet = packet;
    }

    /**
     * 获取关联属性
     * 
     * @param key
     * @return 不会返回null
     */
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return channel.attr(key);
    }

    /**
     * 请求数据包
     * 
     * @return
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * 请求参数。一般用法是将数据包解析为具体的ProtocolBuffer消息类型然后设置为请求参数。
     * 
     * @param requestParameter
     */
    public void setRequestParameter(Object requestParameter) {
        this.requestParameter = requestParameter;
    }

    /**
     * 请求参数
     * 
     * @return 返回null表示该请求没有参数
     */
    public Object getRequestParameter() {
        return requestParameter;
    }

    /**
     * 经过身份验证的用户
     * 
     * @return 返回null表示连接未经过身份验证
     */
    public Object getUser() {
        return user;
    }

    /**
     * 经过身份验证的用户
     * 
     * @param user
     */
    public void setUser(Object user) {
        this.user = user;
    }

    /**
     * 发送应答。应答packet与请求使用相同指令
     * 
     * @param message
     */
    public <T extends Message> void sendResponse(T message) {
        if (message != null) {
            packet.body = message.toByteArray();
            packet.length = (short) packet.body.length;
        }
        packet.length = Packet.HEAD_SIZE;
        channel.writeAndFlush(packet);
    }

    /**
     * 发送应答。应答packet与请求使用相同指令
     * 
     * @param builder
     */
    public <T extends GeneratedMessage.Builder<?>> void sendResponse(T builder) {
        sendResponse(builder.build());
    }

    /**
     * 发送空负载(只有包头)应答。应答packet与请求使用相同指令
     */
    public void sendResponse() {
        sendResponse((Message) null);
    }

    @Override
    public String errorDump() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet.errorDump() + "]";
    }

    @Override
    public String toString() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet + "]";
    }
}
