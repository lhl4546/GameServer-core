/**
 * 
 */
package core.fire.net.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.Config;
import core.fire.util.BaseUtil;
import core.fire.util.ClassUtil;
import io.netty.channel.Channel;

/**
 * @author lhl
 *
 *         2016年3月17日 下午4:06:46
 */
@org.springframework.stereotype.Component
public class HttpServerDispatcher implements Component, HttpHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerDispatcher.class);
    // <uri, 处理器>
    private Map<String, HttpHandler> httpHandlerMap = new HashMap<>();

    @Override
    public void start() throws Exception {
        loadHandler(Config.getString("HTTP_HANDLER_SCAN_PACKAGES"));
        LOG.debug("HttpServerDispatcher start");
    }

    private void loadHandler(String searchPackage) throws Exception {
        if (BaseUtil.isNullOrEmpty(searchPackage)) {
            return;
        }

        String[] packages = BaseUtil.split(searchPackage.trim(), ",");
        for (String onePackage : packages) {
            if (!BaseUtil.isNullOrEmpty(onePackage)) {
                LOG.debug("Load http handler from package {}", onePackage);
                List<Class<?>> classList = ClassUtil.getClasses(onePackage);
                for (Class<?> handler : classList) {
                    HttpRequestHandler annotation = handler.getAnnotation(HttpRequestHandler.class);
                    if (annotation != null) {
                        String uri = annotation.uri();
                        HttpHandler handlerInstance = (HttpHandler) handler.newInstance();
                        addHandler(uri, handlerInstance);
                    }
                }
            }
        }
    }

    /**
     * 注册指令处理器
     * 
     * @param uri
     * @param handler
     * @return 若该指令已注册过则返回之前注册的处理器，否则返回null
     * @throws IllegalStateException
     */
    private void addHandler(String uri, HttpHandler handler) throws IllegalStateException {
        HttpHandler oldHandler = httpHandlerMap.put(uri, handler);
        if (oldHandler != null) {
            throw new IllegalStateException("Duplicate handler for uri " + uri + ", old: "
                    + oldHandler.getClass().getName() + ", new: " + handler.getClass().getName());
        }
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void handle(Channel ch, String uri, Map<String, List<String>> parameter) {
        LOG.debug("IP:{}, uri:{}, parameter:{}", ch.remoteAddress(), uri, parameter);

        HttpHandler handler = httpHandlerMap.get(uri);
        if (handler == null) {
            LOG.warn("No handler found for uri {}, channel will be closed", uri);
            ch.close();
            return;
        }

        handler.handle(ch, uri, parameter);
    }
}
