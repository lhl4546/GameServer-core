/**
 * 
 */
package core.fire.net.tcp;

import core.fire.executor.TcpDispatcher;
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

    public NettyChannelInitializer(TcpDispatcher dispatcher, CodecFactory codecFactory) {
        this.netHandler = new NettyHandler(dispatcher);
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
