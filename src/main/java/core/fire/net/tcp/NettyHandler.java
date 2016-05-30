/**
 * 
 */
package core.fire.net.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import core.fire.executor.DispatcherHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 基于Netty实现的网络IO处理器
 * 
 * @author lhl
 *
 *         2016年1月29日 下午5:36:23
 */
@Sharable
@Component
@Scope("prototype")
public class NettyHandler extends SimpleChannelInboundHandler<Packet>
{
    private static final Logger LOG = LoggerFactory.getLogger(NettyHandler.class);

    @Autowired
    private DispatcherHandler dispatcher;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
        dispatcher.handle(ctx.channel(), msg);
    }
}
