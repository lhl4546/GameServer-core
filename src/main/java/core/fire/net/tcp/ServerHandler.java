/**
 *
 */
package core.fire.net.tcp;

import core.fire.user.GroupManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Netty实现的网络IO处理器。网络消息将被TcpDispatcher分发处理
 *
 * @author lhl
 *         <p>
 *         2016年1月29日 下午5:36:23
 */
@Sharable
public class ServerHandler extends SimpleChannelInboundHandler<IPacket> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

    private TcpDispatcher dispatcher;

    public ServerHandler(TcpDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        GroupManager.INSTANCE.addToGlobal(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        GroupManager.INSTANCE.removeFromGlobal(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("", cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket msg) throws Exception {
        dispatcher.handle(ctx.channel(), msg);
    }

    /**
     * 心跳由客户端主动发起，连续3次
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    }
}
