/**
 * 
 */
package core.fire.net.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;

/**
 * @author lhl
 *
 *         2016年1月30日 上午9:29:24
 */
public class NettyChannelInitializer extends ChannelInitializer<Channel>
{
    private NettyHandler netHandler = new NettyHandler();
    private ChannelOutboundHandler encoder = new PlainProtocolEncoder();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ENCODER", encoder);
        pipeline.addLast("DECODER", new PlainProtocolDecoder());
        pipeline.addLast("NET_HANDLER", netHandler);
    }
}
