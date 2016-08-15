/**
 * 
 */
package core.fire.net.tcp;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import core.fire.Component;
import core.fire.CoreServer;
import core.fire.Dumpable;
import core.fire.collection.IntHashMap;
import core.fire.executor.Sequence;
import core.fire.util.ClassUtil;
import core.fire.util.Util;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 请求派发处理器，负责将网络IO传过来的请求分发给指定处理器处理
 * <P>
 * 对于每一个请求，都将按以下流程顺序处理
 * <ol>
 * <li>拦截请求</li>
 * <li>处理请求</li>
 * </ol>
 * <p>
 * 所有实现了{@linkplain HandlerInterceptor}
 * 接口的非抽象类都将被自动加载为协议拦截器，拦截器扫描包在CoreConfiguration中配置
 * <p>
 * 所有以@RequestHandler注解的类都将被自动加载为TCP协议处理类，处理器扫描包在CoreConfiguration中配置
 * 
 * @author lhl
 *
 *         2016年1月30日 下午3:49:52
 */
public final class TcpDispatcher implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(TcpDispatcher.class);
    // <指令，处理器>
    private IntHashMap<Handler> handlerMap = new IntHashMap<>();
    // <指令，请求参数类型>
    private IntHashMap<GeneratedMessage> requestParamType = new IntHashMap<>();
    // 指令处理线程池
    private ExecutorService executor;
    // 消息队列将被作为附件设置到channel上
    // 绑定了消息队列的session的事件将被提交到消息队列执行，否则统一提交到公用消息队列
    public static final AttributeKey<Sequence> SEQUENCE_KEY = AttributeKey.valueOf("SEQUENCE_KEY");
    // 请求拦截器
    private HandlerInterceptor[] interceptors;
    //
    private CoreServer core;

    public TcpDispatcher(CoreServer core) {
        this.core = core;
        this.executor = core.getTcpExecutor();
    }

    public void handle(Channel channel, IPacket packet) {
        doDispatch(channel, packet);
    }

    /**
     * 派发请求到指定处理器，并将处理逻辑提交给线程池异步处理
     * 
     * @param channel
     * @param packet
     */
    protected void doDispatch(Channel channel, IPacket packet) {
        Handler handler = handlerMap.get(packet.getCode());
        if (handler == null) {
            LOG.warn("No handler found for code {}, session will be closed", packet.getCode());
            channel.close();
            return;
        }

        SocketRequest request = new SocketRequest(channel, packet);
        RunnableTask task = new RunnableTask(request, handler);

        Sequence sequence = channel.attr(SEQUENCE_KEY).get();
        if (sequence != null) {
            sequence.addTask(task);
        } else {
            executor.execute(task);
        }
    }

    /**
     * 获取指定协议的参数类型
     * 
     * @param code 协议指令
     * @return 返回null表示没有该指令对应的请求参数类型
     */
    public GeneratedMessage getParamType(short code) {
        return requestParamType.get(code);
    }

    @Override
    public void start() throws Exception {
        loadHandler(core.getTcpHandlerScanPath());
        loadInterceptor(core.getTcpInterceptorScanPath());
        LOG.debug("TcpDispatcher start");
    }

    /**
     * 加载指令处理器
     * 
     * @param searchPackage 支持使用英文逗号分隔多个包
     * @throws Exception
     */
    private void loadHandler(String searchPackage) throws Exception {
        LOG.debug("Load handler from packages {}", searchPackage);

        doLoad(searchPackage, clas -> {
            try {
                if (clas.isAnnotationPresent(TcpRequestHandler.class)) {
                    TcpRequestHandler annotation = clas.getAnnotation(TcpRequestHandler.class);
                    short code = annotation.code();
                    Handler handlerInstance = (Handler) clas.newInstance();
                    addHandler(code, handlerInstance);
                    Class<? extends GeneratedMessage> paramType = annotation.requestParamType();
                    GeneratedMessage paramInstance = instantiate(paramType);
                    addParamType(code, paramInstance);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        LOG.debug("{} handler has been loaded", handlerMap.size());
    }

    /**
     * 注册指令处理器
     * 
     * @param code
     * @param handler
     * @return 若该指令已注册过则返回之前注册的处理器，否则返回null
     * @throws IllegalStateException
     */
    private void addHandler(short code, Handler handler) throws IllegalStateException {
        Handler oldHandler = handlerMap.put(code, handler);
        if (oldHandler != null) {
            throw new IllegalStateException("Duplicate handler for code " + code + ", old: " + oldHandler.getClass().getName() + ", new: " + handler.getClass().getName());
        }
    }

    /**
     * 注册请求参数类型
     * 
     * @param code
     * @param param
     */
    private void addParamType(short code, GeneratedMessage param) {
        requestParamType.put(code, param);
    }

    // 每个生成的PB协议类都应该有一个getDefaultInstance静态方法
    private GeneratedMessage instantiate(Class<? extends GeneratedMessage> type) throws Exception {
        Method method = type.getMethod("getDefaultInstance");
        return (GeneratedMessage) method.invoke(type);
    }

    /**
     * 加载协议拦截器
     * 
     * @param scanPackages 支持使用英文逗号分隔多个包
     * @throws Exception
     */
    private void loadInterceptor(String scanPackages) throws Exception {
        LOG.debug("Load interceptor from packages {}", scanPackages);

        Class<HandlerInterceptor> interceptorClass = HandlerInterceptor.class;
        List<HandlerInterceptor> interceptorList = new ArrayList<>();
        doLoad(scanPackages, clas -> {
            try {
                if (interceptorClass.isAssignableFrom(clas) && clas != interceptorClass && !Modifier.isAbstract(clas.getModifiers())) {
                    HandlerInterceptor interceptor = (HandlerInterceptor) clas.newInstance();
                    interceptorList.add(interceptor);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 默认注册参数类型拦截器
        interceptorList.add(new ParameterTypeInterceptor(this));
        interceptorList.sort(null);
        LOG.debug("After sort, handler interceptor list is {}", interceptorList);
        this.interceptors = interceptorList.toArray(new HandlerInterceptor[interceptorList.size()]);
        LOG.debug("{} interceptors has been loaded", interceptors.length);
    }

    /**
     * 统一加载逻辑，自定义目标类遍历方法
     * 
     * @param scanPackages
     * @param consumer
     * @throws Exception
     */
    private void doLoad(String scanPackages, Consumer<Class<?>> consumer) throws Exception {
        if (Util.isNullOrEmpty(scanPackages)) {
            return;
        }

        String[] packages = Util.split(scanPackages, ",");
        for (String onePackage : packages) {
            List<Class<?>> classList = ClassUtil.getClasses(onePackage);
            classList.forEach(consumer);
        }
    }

    @Override
    public void stop() {
        Util.shutdownThreadPool(executor, 5 * 1000);
        LOG.debug("TcpDispatcher stop");
    }

    class RunnableTask implements Runnable, Dumpable
    {
        SocketRequest request;
        Handler handler;

        RunnableTask(SocketRequest request, Handler handler) {
            this.request = request;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // 拦截
                HandlerInterceptor[] interceptors = TcpDispatcher.this.interceptors;
                for (int i = 0; i < interceptors.length; i++) {
                    HandlerInterceptor interceptor = interceptors[i];
                    if (!interceptor.preHandle(request)) {
                        return;
                    }
                }

                // 处理
                handler.handle(request);
            } catch (Throwable t) {
                LOG.error("{}", dump(), t);
            }
        }

        @Override
        public String toString() {
            return "RunnableTask: [request=" + request + "]";
        }

        @Override
        public String dump() {
            return "RunnableTask: [request=" + request.dump() + "]";
        }
    }
}
