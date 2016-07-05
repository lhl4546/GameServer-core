/**
 * 
 */
package core.fire.rpc.json.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import core.fire.Component;
import core.fire.net.http.HttpHandler;
import core.fire.util.ClassUtil;
import core.fire.util.Util;
import io.netty.channel.Channel;

/**
 * RPC请求派发器
 * 
 * @author lihuoliang
 *
 */
public class RpcDispatcher implements HttpHandler, Component
{
    private static final Logger LOG = LoggerFactory.getLogger(RpcDispatcher.class);
    // 请求处理器映射，key=methodName
    private Map<String, RpcHandler<?>> handlerMap = new HashMap<>();
    // 请求处理器扫描包，多个包使用英文逗号分隔
    private String handlerScanPackages;

    /**
     * @param scanPackages rpc处理器扫描包，多个包使用英文逗号分隔
     */
    public RpcDispatcher(String scanPackages) {
        this.handlerScanPackages = scanPackages;
    }

    @Override
    public void handle(Channel channel, Map<String, List<String>> parameter) {
        List<String> requestString = parameter.get("request");
        if (Util.isNullOrEmpty(requestString)) {
            LOG.warn("No request found in querystring, close the channel");
            channel.close();
            return;
        }

        RpcRequest request = JSON.parseObject(requestString.get(0), RpcRequest.class);
        RpcHandler<?> handler = handlerMap.get(request.getMethodName());
        if (handler == null) {
            LOG.warn("No handler found for method {}, close the channel", request.getMethodName());
            channel.close();
            return;
        }

        handler.handle(channel, request);
    }

    /**
     * 加载指令处理器
     * 
     * @param searchPackage 搜索包名，多个包名使用逗号分割
     * @throws Exception
     */
    private void loadHandler(String searchPackage) throws Exception {
        if (Util.isNullOrEmpty(searchPackage)) {
            return;
        }

        String[] packages = Util.split(searchPackage.trim(), ",");
        for (String onePackage : packages) {
            if (!Util.isNullOrEmpty(onePackage)) {
                LOG.debug("Load rpc handler from package {}", onePackage);
                List<Class<?>> classList = ClassUtil.getClasses(onePackage);
                for (Class<?> handler : classList) {
                    RpcRequestHandler annotation = handler.getAnnotation(RpcRequestHandler.class);
                    if (annotation != null) {
                        String methodName = annotation.methodName();
                        addHandler(methodName, (RpcHandler<?>) handler.newInstance());
                    }
                }
            }
        }
    }

    /**
     * 
     * 注册方法处理器
     * 
     * @param methodName 方法名
     * @param handler RPC处理器
     */
    public void addHandler(String methodName, RpcHandler<?> handler) {
        RpcHandler<?> oldHandler = handlerMap.put(methodName, handler);
        if (oldHandler != null) {
            throw new IllegalStateException("Duplicate handler for method " + methodName + ", old: " + oldHandler.getClass().getName() + ", new: " + handler.getClass().getName());
        }
    }

    @Override
    public void start() throws Exception {
        loadHandler(handlerScanPackages);
        LOG.debug("RPCDispatcher start");
    }

    @Override
    public void stop() throws Exception {
        LOG.debug("RPCDispatcher stop");
    }
}
