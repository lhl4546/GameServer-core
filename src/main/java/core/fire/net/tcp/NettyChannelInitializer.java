/**
 * 
 */
package core.fire.net.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * @author lhl
 *
 *         2016年1月30日 上午9:29:24
 */
public class NettyChannelInitializer extends ChannelInitializer<Channel>
{
    private NettyHandler netHandler;
    private CodecFactory codecFactory;

    public NettyChannelInitializer(NettyHandler netHandler, CodecFactory codecFactory) {
        this.netHandler = netHandler;
        this.codecFactory = codecFactory;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ENCODER", codecFactory.getEncoder());
        pipeline.addLast("DECODER", codecFactory.getDecoder());
        pipeline.addLast("NET_HANDLER", netHandler);
    }
}
