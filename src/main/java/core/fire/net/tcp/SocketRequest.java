package core.fire.net.tcp;

import core.fire.Dumpable;
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
    private IPacket packet;
    private Object requestParameter;
    private Object user;

    public SocketRequest(Channel channel, IPacket packet) {
        this.channel = channel;
        this.packet = packet;
    }

    /**
     * 关联属性
     * 
     * @param key
     * @return 不会返回null
     */
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return channel.attr(key);
    }

    /**
     * 获取网络连接
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
    public IPacket getPacket() {
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
     * 请求参数，支持返回GeneratedMessage子类型
     * 
     * @return 返回null表示该请求没有参数
     */
    @SuppressWarnings("unchecked")
    public <ParamType> ParamType getRequestParameter() {
        return (ParamType) requestParameter;
    }

    /**
     * 经过身份验证的用户，支持返回自定义用户类型(与setUser的参数类型保持一致)
     * 
     * @return 返回null表示连接未经过身份验证
     */
    @SuppressWarnings("unchecked")
    public <UserType> UserType getUser() {
        return (UserType) user;
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
    public String dump() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet.dump() + "]";
    }

    @Override
    public String toString() {
        return "SocketRequest: [channel=" + channel + ", packet=" + packet + "]";
    }
}
