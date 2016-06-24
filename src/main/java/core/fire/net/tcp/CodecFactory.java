package core.fire.net.tcp;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

public interface CodecFactory
{
    ChannelInboundHandler getDecoder();

    ChannelOutboundHandler getEncoder();
}
