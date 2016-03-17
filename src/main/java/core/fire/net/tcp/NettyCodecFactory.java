package core.fire.net.tcp;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编解码工厂
 * 
 * @author lhl
 *
 *         2015年11月13日 下午2:38:07
 */
public interface NettyCodecFactory
{
    /**
     * 解码器
     * 
     * @return
     */
    ByteToMessageDecoder getDecoder();

    /**
     * 编码器
     * 
     * @return
     */
    MessageToByteEncoder<?> getEncoder();
}
