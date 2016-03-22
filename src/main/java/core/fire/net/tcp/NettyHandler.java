/**
 * 
 */
package core.fire.net.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import core.fire.executor.DispatcherHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * 基于Netty实现的网络IO处理器
 * 
 * @author lhl
 *
 *         2016年1月29日 下午5:36:23
 */
@Sharable
public class NettyHandler extends SimpleChannelInboundHandler<Packet>
{
    private static final Logger LOG = LoggerFactory.getLogger(NettyHandler.class);
    private static final AttributeKey<NetSession> SESSION_KEY = AttributeKey.valueOf("NET_SESSION");

    @Autowired
    private DispatcherHandler dispatcher;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetSession netSession = new NettySession(ctx.channel());
        ctx.attr(SESSION_KEY).set(netSession);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.debug("Exception caught: {}", ctx.channel().remoteAddress(), cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        NetSession netSession = ctx.attr(SESSION_KEY).get();
        dispatcher.handle(netSession, msg);
    }
}
