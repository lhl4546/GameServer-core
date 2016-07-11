/**
 * 
 */
package core.fire.user;

import java.util.ArrayList;
import java.util.List;

import core.fire.net.tcp.Packet;

/**
 * 用户组。非线程安全
 * 
 * @author lihuoliang
 *
 */
public class UserGroup
{
    private List<User> users = new ArrayList<>();

    public void addUser(User user) {
        users.add(user);
    }

    public void remove(User user) {
        users.remove(user);
    }

    /**
     * 组内广播消息
     * 
     * @param packet
     */
    public void broadcast(Packet packet) {
        for (User user : users) {
            user.send(packet);
        }
    }
}
