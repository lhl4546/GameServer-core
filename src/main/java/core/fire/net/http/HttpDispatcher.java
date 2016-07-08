package core.fire.net.http;

import static core.fire.net.http.HttpInboundHandler.KEY_PATH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.util.ClassUtil;
import core.fire.util.Util;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * http请求分发器
 * 
 * @author lhl
 *
 *         2016年3月28日 下午4:38:37
 */
public class HttpDispatcher implements Component, HttpHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpDispatcher.class);
    // 请求处理器映射，key=uri
    private Map<String, HttpHandler> handlerMap = new HashMap<>();
    // 请求处理器扫描包，多个包使用英文逗号分隔
    private String handlerScanPackages;
    // 请求处理线程池
    private ExecutorService executor;

    /**
     * @param handleScanPackages 请求扫描包，多个包使用英文逗号分隔
     */
    public HttpDispatcher(String handleScanPackages) {
        this.handlerScanPackages = handleScanPackages;
    }

    /**
     * @param handlerScanPackages 请求扫描包，多个包使用英文逗号分隔
     * @param executor 请求将提交给这个线程池处理
     */
    public HttpDispatcher(String handlerScanPackages, ExecutorService executor) {
        this.handlerScanPackages = handlerScanPackages;
        this.executor = executor;
    }

    @Override
    public void handle(Channel channel, Map<String, List<String>> parameter) {
        String uri = channel.attr(KEY_PATH).get();
        HttpHandler handler = handlerMap.get(uri);
        if (handler == null) {
            LOG.warn("No handler found for uri {}, session will be close", uri);
            // 此处如果返回404会导致基于UrlConnection的HttpUtil抛出FileNotFound异常，但是基于Apache的HttpClient则不会
            sendError(channel, "Not found", HttpResponseStatus.BAD_REQUEST);
            return;
        }

        LOG.debug("Request ip: {}, uri: {}, parameter: {}", channel.remoteAddress(), uri, parameter);

        if (executor != null) {
            Runnable task = () -> handler.handle(channel, parameter);
            executor.submit(task);
        } else {
            handler.handle(channel, parameter);
        }
    }

    /**
     * 发送应答信息。发送完毕后将会关闭连接
     * 
     * @param channel
     * @param message 描述信息
     * @param status HTTP状态码
     */
    private void sendError(Channel channel, String message, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(message.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void start() throws Exception {
        loadHandler(handlerScanPackages);
        LOG.debug("HttpDispatcher start");
    }

    /**
     * 加载指令处理器
     * 
     * @param searchPackage 搜索包名，多个包名使用逗号分割
     * @throws Exception
     */
    protected void loadHandler(String searchPackage) throws Exception {
        if (Util.isNullOrEmpty(searchPackage)) {
            return;
        }

        String[] packages = Util.split(searchPackage.trim(), ",");
        for (String onePackage : packages) {
            if (!Util.isNullOrEmpty(onePackage)) {
                LOG.debug("Load http handler from package {}", onePackage);
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

    /**
     * 添加uri与对应的处理器映射
     * 
     * @param uri
     * @param handler
     */
    public void addHandler(String uri, HttpHandler handler) {
        HttpHandler oldHandler = handlerMap.put(uri, handler);
        if (oldHandler != null) {
            throw new IllegalStateException("Duplicate handler for uri " + uri + ", old: " + oldHandler.getClass().getName() + ", new: " + handler.getClass().getName());
        }
    }

    @Override
    public void stop() throws Exception {
        if (executor != null) {
            executor.shutdownNow();
        }
        LOG.debug("HttpDispatcher stop");
    }
}
