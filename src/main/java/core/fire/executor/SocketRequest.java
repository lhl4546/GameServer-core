package core.fire.executor;

import core.fire.Dumpable;
import core.fire.net.tcp.Packet;
import io.netty.channel.Channel;

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
     * 网络连接
     * 
     * @return
     */
    public Channel getChannel() {
        return channel;
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

    @Override
    public String errorDump() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet.errorDump() + "]";
    }

    @Override
    public String toString() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet + "]";
    }
}
