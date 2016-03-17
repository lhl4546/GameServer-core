/**
 * 
 */
package core.fire.net.tcp;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.fire.net.NetSession;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author lhl
 *
 *         2016年1月29日 下午5:41:05
 */
public class NettySession implements NetSession
{
    private Channel channel;

    private static final Map<String, AttributeKey<Object>> keyMap = new ConcurrentHashMap<>();

    public NettySession(Channel channel) {
        this.channel = channel;
    }

    @Override
    public long getId() {
        return 0L;
    }

    @Override
    public void send(Object message) {
        channel.writeAndFlush(message);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public void setAttachment(String key, Object val) {
        AttributeKey<Object> attrKey = keyMap.computeIfAbsent(key, k -> {
            return AttributeKey.valueOf(key);
        });
        channel.attr(attrKey).set(val);
    }

    @Override
    public Object getAttachment(String key) {
        AttributeKey<Object> attrKey = keyMap.get(key);
        if (attrKey != null) {
            return channel.attr(attrKey).get();
        }

        return null;
    }

    @Override
    public String toString() {
        return channel == null ? "null" : channel.remoteAddress().toString();
    }
}
