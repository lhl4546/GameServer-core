/**
 * 
 */
package core.fire.net.tcp;

import io.netty.channel.Channel;

/**
 * @author lihuoliang
 *
 */
public class IOUtil
{
    public static void send(SocketRequest request, IPacket packet) {
        send(request.getChannel(), packet);
    }

    public static void send(Channel channel, IPacket packet) {
        channel.writeAndFlush(packet);
    }
}
