package core.fire.net.netty;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 明文协议编解码工厂
 * 
 * @author lhl
 *
 *         2015年11月23日 下午6:15:24
 */
public class PlainCodecFactory implements NettyCodecFactory
{
    @Override
    public ByteToMessageDecoder getDecoder() {
        return new PlainProtocolDecoder();
    }

    @Override
    public MessageToByteEncoder<?> getEncoder() {
        return new PlainProtocolEncoder();
    }
}
