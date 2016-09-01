/**
 * 
 */
package core.fire.net.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * GET/POST请求处理器。发送应答是子类的责任
 * 
 * @author lhl
 *
 * 2016年3月28日 下午3:51:19
 */
public interface HttpHandler
{
    /**
     * Http请求处理接口(GET/POST)
     * 
     * @param channel
     * @param parameter
     */
    void handle(Channel channel, Map<String, List<String>> parameter);

    /**
     * 发送应答
     * 
     * @param channel
     * @param data
     */
    default void sendResponse(Channel channel, String data) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(data.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        channel.writeAndFlush(response);
    }
}
