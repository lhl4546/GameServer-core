/**
 * 
 */
package core.fire.net.http;

import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author lhl
 *
 *         2016年3月17日 下午4:24:33
 */
public abstract class AbstractHttpHandler implements HttpHandler
{
    @Override
    public void handle(Channel ch, String uri, Map<String, List<String>> parameter) {
        String returnString = handle(parameter);
        FullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(returnString.getBytes()));
        rsp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        rsp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, rsp.content().readableBytes());
        ch.write(rsp).addListener(ChannelFutureListener.CLOSE);
    }

    protected abstract String handle(Map<String, List<String>> parameter);
}
