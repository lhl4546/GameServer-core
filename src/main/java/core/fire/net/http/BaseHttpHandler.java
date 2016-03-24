/**
 * 
 */
package core.fire.net.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * HTTP处理器基类。 给自定义处理器加上 {@linkplain HttpRequestHandler}注解以实现自动加载处理器
 * 
 * @author lhl
 *
 *         2016年3月17日 下午4:24:33
 */
public abstract class BaseHttpHandler implements HttpHandler
{
    /**
     * 返回错误
     * 
     * @param ch
     * @param status
     */
    protected void sendError(Channel ch, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
                Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ch.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 返回应答
     * 
     * @param ch
     * @param responseString
     */
    protected void sendResponse(Channel ch, String responseString) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseString.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ch.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
