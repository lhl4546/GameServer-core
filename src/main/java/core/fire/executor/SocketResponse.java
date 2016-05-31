package core.fire.executor;

import core.fire.Dumpable;
import core.fire.net.tcp.Packet;
import io.netty.channel.Channel;

public class SocketResponse implements Dumpable
{
    /** 网络连接 */
    private Channel channel;

    public SocketResponse(Channel channel) {
        this.channel = channel;
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
     * 发送应答
     * 
     * @param responseData
     */
    public void sendResponse(Packet responseData) {
        channel.writeAndFlush(responseData);
    }

    @Override
    public String errorDump() {
        return toString();
    }

    @Override
    public String toString() {
        return "SocketResponse: [channel=" + channel + "]";
    }
}
