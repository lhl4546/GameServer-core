/**
 *
 */
package core.fire.user;

import java.util.Collection;
import java.util.Iterator;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 用户组
 *
 * @author lihuoliang
 */
public class UserGroup implements ChannelGroup
{

    private ChannelGroup group;

    public UserGroup(String name) {
        group = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    @Override
    public String name() {
        return group.name();
    }

    @Override
    public ChannelGroupFuture write(Object message) {
        return group.write(message);
    }

    @Override
    public ChannelGroupFuture write(Object message, ChannelMatcher matcher) {
        return group.write(message, matcher);
    }

    @Override
    public ChannelGroup flush() {
        return group.flush();
    }

    @Override
    public ChannelGroup flush(ChannelMatcher matcher) {
        return group.flush(matcher);
    }

    @Override
    public ChannelGroupFuture writeAndFlush(Object message) {
        return group.writeAndFlush(message);
    }

    @Deprecated
    @Override
    public ChannelGroupFuture flushAndWrite(Object message) {
        return group.flushAndWrite(message);
    }

    @Override
    public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher) {
        return group.writeAndFlush(message, matcher);
    }

    @Deprecated
    @Override
    public ChannelGroupFuture flushAndWrite(Object message, ChannelMatcher matcher) {
        return group.flushAndWrite(message, matcher);
    }

    @Override
    public ChannelGroupFuture disconnect() {
        return group.disconnect();
    }

    @Override
    public ChannelGroupFuture disconnect(ChannelMatcher matcher) {
        return group.disconnect(matcher);
    }

    @Override
    public ChannelGroupFuture close() {
        return group.close();
    }

    @Override
    public ChannelGroupFuture close(ChannelMatcher matcher) {
        return group.close(matcher);
    }

    @Deprecated
    @Override
    public ChannelGroupFuture deregister() {
        return group.deregister();
    }

    @Deprecated
    @Override
    public ChannelGroupFuture deregister(ChannelMatcher matcher) {
        return group.deregister(matcher);
    }

    @Override
    public int compareTo(ChannelGroup o) {
        return group.compareTo(o);
    }

    @Override
    public int size() {
        return group.size();
    }

    @Override
    public boolean isEmpty() {
        return group.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return group.contains(o);
    }

    @Override
    public Iterator<Channel> iterator() {
        return group.iterator();
    }

    @Override
    public Object[] toArray() {
        return group.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return group.toArray(a);
    }

    @Override
    public boolean add(Channel channel) {
        return group.add(channel);
    }

    @Override
    public boolean remove(Object o) {
        return group.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return group.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Channel> c) {
        return group.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return group.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return group.removeAll(c);
    }

    @Override
    public void clear() {
        group.clear();
    }
}
