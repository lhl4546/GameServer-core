/**
 * 
 */
package core.fire.net.tcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * @author lhl
 *
 *         2016年1月30日 上午9:29:24
 */
@Component
public class NettyChannelInitializer extends ChannelInitializer<Channel>
{
    @Autowired
    private NettyHandler netHandler;
    @Autowired
    private PlainProtocolEncoder encoder;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ENCODER", encoder);
        pipeline.addLast("DECODER", new PlainProtocolDecoder());
        pipeline.addLast("NET_HANDLER", netHandler);
    }
}
