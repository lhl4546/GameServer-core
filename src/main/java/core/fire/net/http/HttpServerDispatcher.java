package core.fire.net.http;

import static core.fire.net.http.HttpInboundHandler.KEY_PATH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.Config;
import core.fire.util.BaseUtil;
import core.fire.util.ClassUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Http请求分发器
 * 
 * @author lhl
 *
 *         2016年3月28日 下午4:38:37
 */
public class HttpServerDispatcher implements Component, HttpHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerDispatcher.class);
    private Map<String, HttpHandler> handlerMap;

    public HttpServerDispatcher() {
        handlerMap = new HashMap<>();
    }

    @Override
    public void handle(Channel channel, Map<String, List<String>> parameter) {
        String uri = channel.attr(KEY_PATH).get();
        HttpHandler handler = handlerMap.get(uri);
        if (handler == null) {
            LOG.warn("No handler found for uri {}, session will be close", uri);
            sendError(channel, "Not found", HttpResponseStatus.NOT_FOUND);
            return;
        }

        LOG.debug("Request ip: {}, uri: {}, parameter: {}", channel.remoteAddress(), uri, parameter);
        handler.handle(channel, parameter);
    }

    /**
     * 发送应答信息。发送完毕后将会关闭连接
     * 
     * @param channel
     * @param message 描述信息
     * @param status HTTP状态码
     */
    private void sendError(Channel channel, String message, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(message.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void start() throws Exception {
        loadHandler(Config.getString("HANDLER_SCAN_PACKAGES"));
        LOG.debug("HttpServerDispatcher start");
    }

    /**
     * 加载指令处理器
     * 
     * @param searchPackage 搜索包名，多个包名使用逗号分割
     * @throws Exception
     */
    private void loadHandler(String searchPackage) throws Exception {
        if (BaseUtil.isNullOrEmpty(searchPackage)) {
            return;
        }

        String[] packages = BaseUtil.split(searchPackage.trim(), ",");
        for (String onePackage : packages) {
            if (!BaseUtil.isNullOrEmpty(onePackage)) {
                LOG.debug("Load handler from package {}", onePackage);
                List<Class<?>> classList = ClassUtil.getClasses(onePackage);
                for (Class<?> handler : classList) {
                    HttpRequestHandler annotation = handler.getAnnotation(HttpRequestHandler.class);
                    if (annotation != null && annotation.isEnabled()) {
                        String path = annotation.path();
                        addHandler(path, (HttpHandler) handler.newInstance());
                    }
                }
            }
        }
    }

    private void addHandler(String uri, HttpHandler handler) {
        HttpHandler oldHandler = handlerMap.put(uri, handler);
        if (oldHandler != null) {
            throw new IllegalStateException("Duplicate handler for uri " + uri + ", old: "
                    + oldHandler.getClass().getName() + ", new: " + handler.getClass().getName());
        }
    }

    @Override
    public void stop() throws Exception {
        LOG.debug("HttpServerDispatcher stop");
    }
}
