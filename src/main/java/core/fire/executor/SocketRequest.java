package core.fire.executor;

import core.fire.Dumpable;
import core.fire.net.tcp.Packet;
import io.netty.channel.Channel;

public class SocketRequest implements Dumpable
{
    /** 网络连接 */
    private Channel channel;
    /** 请求数据包 */
    private Packet packet;
    /** 请求参数 */
    private Object requestParameter;
    /** 经过身份验证的用户 */
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
     * 请求参数
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
