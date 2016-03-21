/**
 * 
 */
package core.fire.net.http;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * HTTP处理器基类，自定义处理器继承此类并实现{@linkplain #handle(Map)}方法。 给自定义处理器加上
 * {@linkplain HttpRequestHandler}注解以实现自动加载处理器
 * 
 * @author lhl
 *
 *         2016年3月17日 下午4:24:33
 */
public abstract class BaseHttpHandler implements HttpHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(BaseHttpHandler.class);
    private static final String ERROR_STRING = "{}";

    @Override
    public void handle(Channel ch, String uri, Map<String, List<String>> parameter) {
        String returnString = null;
        try {
            returnString = handle(parameter);
        } catch (Exception e) {
            LOG.error("", e);
            returnString = ERROR_STRING;
        }

        FullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(returnString.getBytes()));
        rsp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        rsp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, rsp.content().readableBytes());
        ch.write(rsp).addListener(ChannelFutureListener.CLOSE);
    }

    protected abstract String handle(Map<String, List<String>> parameter);
}
