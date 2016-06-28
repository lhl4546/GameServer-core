/**
 * 
 */
package core.fire.executor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import core.fire.Component;
import core.fire.CoreConfiguration;
import core.fire.Dumpable;
import core.fire.NamedThreadFactory;
import core.fire.net.tcp.Packet;
import core.fire.util.ClassUtil;
import core.fire.util.IntHashMap;
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
 * 所有实现了{@linkplain HandlerInterceptor}接口的非抽象类都将被自动加载为协议拦截器
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

    private CoreConfiguration config;

    public TcpDispatcher(CoreConfiguration config) {
        this.config = config;
        initLogicThreadPool();
    }

    private void initLogicThreadPool() {
        int threads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(threads, new NamedThreadFactory("logic"));
    }

    public void handle(Channel channel, Packet packet) {
        doDispatch(channel, packet);
    }

    /**
     * 派发请求到指定处理器，并将处理逻辑提交给线程池异步处理
     * 
     * @param channel
     * @param packet
     */
    protected void doDispatch(Channel channel, Packet packet) {
        Handler handler = handlerMap.get(packet.code);
        if (handler == null) {
            LOG.warn("No handler found for code {}, session will be closed", packet.code);
            channel.close();
            return;
        }

        SocketRequest request = new SocketRequest(channel, packet);
        SocketResponse response = new SocketResponse(channel);
        RunnableTask task = new RunnableTask(request, response, handler);

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

    /**
     * 生成消息队列
     * 
     * @return
     */
    public Sequence newSequence() {
        return new Sequence(executor);
    }

    @Override
    public void start() throws Exception {
        loadHandler(config.getTcpHandlerScanPackages());
        loadInterceptor(config.getTcpInterceptorScanPackages());
        LOG.debug("DispatcherHandler start");
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
                if (clas.isAnnotationPresent(RequestHandler.class)) {
                    RequestHandler annotation = clas.getAnnotation(RequestHandler.class);
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
        LOG.debug("DispatcherHandler stop");
    }

    class RunnableTask implements Runnable, Dumpable
    {
        SocketRequest request;
        SocketResponse response;
        Handler handler;

        RunnableTask(SocketRequest request, SocketResponse response, Handler handler) {
            this.request = request;
            this.response = response;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // 拦截
                HandlerInterceptor[] interceptors = TcpDispatcher.this.interceptors;
                for (int i = 0; i < interceptors.length; i++) {
                    HandlerInterceptor interceptor = interceptors[i];
                    if (!interceptor.preHandle(request, response)) {
                        return;
                    }
                }

                // 处理
                handler.handle(request, response);
            } catch (Throwable t) {
                LOG.error("{}", errorDump(), t);
            }
        }

        @Override
        public String toString() {
            return "RunnableTask: [request=" + request + ", response=" + response + "]";
        }

        @Override
        public String errorDump() {
            return "RunnableTask: [request=" + request.errorDump() + ", response=" + response.errorDump() + "]";
        }
    }
}
