/**
 * 
 */
package core.fire.net.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author lhl
 *
 * 2016年1月30日 上午9:29:24
 */
public class ServerChannelInitializer extends ChannelInitializer<Channel>
{
    private ServerHandler netHandler;
    private CodecFactory codecFactory;

    public ServerChannelInitializer() {
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.netHandler = serverHandler;
    }

    public void setCodecFactory(CodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ENCODER", codecFactory.getEncoder());
        pipeline.addLast("DECODER", codecFactory.getDecoder());
        pipeline.addLast(new IdleStateHandler(30, 0, 0));
        pipeline.addLast("NET_HANDLER", netHandler);
    }
}
