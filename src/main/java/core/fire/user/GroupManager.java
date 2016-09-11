package core.fire.user;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lihuoliang on 2016/9/11.
 */
public enum GroupManager {
    INSTANCE;

    // 全局用户组，包含所有已建立连接的用户
    private ChannelGroup globalGroup;
    // 用户自定义分组，key为分组依据
    private ConcurrentMap<String, UserGroup> groups;

    GroupManager() {
        globalGroup = new UserGroup("global-user-group", GlobalEventExecutor.INSTANCE);
        groups = new ConcurrentHashMap<>();
    }

    /**
     * 加入全局用户组
     *
     * @param channel
     */
    public void addToGlobal(Channel channel) {
        globalGroup.add(channel);
    }

    /**
     * 移除出全局用户组
     *
     * @param channel
     */
    public void removeFromGlobal(Channel channel) {
        globalGroup.remove(channel);
    }

    /**
     * 全局用户组广播
     *
     * @param message
     */
    public void broadcast(Object message) {
        globalGroup.writeAndFlush(message);
    }

    /**
     * 添加自定义用户组
     *
     * @param name
     * @param group
     */
    public void addGroup(String name, UserGroup group) {
        groups.put(name, group);
    }

    /**
     * 获取自定义用户组
     *
     * @param name
     * @return
     */
    public UserGroup getGroup(String name) {
        return groups.get(name);
    }
}
