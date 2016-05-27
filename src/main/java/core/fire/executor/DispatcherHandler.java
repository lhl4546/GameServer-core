/**
 * 
 */
package core.fire.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import core.fire.Component;
import core.fire.Config;
import core.fire.NamedThreadFactory;
import core.fire.net.tcp.Packet;
import core.fire.util.BaseUtil;
import core.fire.util.ClassUtil;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 请求派发处理器，负责将网络IO传过来的请求分发给指定处理器处理
 * 
 * @author lhl
 *
 *         2016年1月30日 下午3:49:52
 */
@org.springframework.stereotype.Component
public final class DispatcherHandler implements Handler, Component
{
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherHandler.class);
    // <指令，处理器>
    private Map<Short, Handler> handlerMap;
    // <指令，请求参数类型>
    private Map<Short, GeneratedMessage> requestParamType;
    // 指令处理线程池
    private ExecutorService executor;
    // 消息队列将被作为附件设置到channel上
    // 绑定了消息队列的session的事件将被提交到消息队列执行，否则统一提交到公用消息队列
    public static final AttributeKey<Sequence> SEQUENCE_KEY = AttributeKey.valueOf("SEQUENCE_KEY");

    public DispatcherHandler() {
        handlerMap = new HashMap<>();
        requestParamType = new HashMap<>();
        initLogicThreadPool();
    }

    /**
     * 初始化逻辑处理线程池
     */
    private void initLogicThreadPool() {
        int threads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(threads, new NamedThreadFactory("LOGIC"));
    }

    /**
     * 该方法将在Netty I/O线程池中运行
     */
    @Override
    public void handle(Channel channel, Packet packet) {
        Handler handler = handlerMap.get(Short.valueOf(packet.code));
        if (handler == null) {
            LOG.warn("No handler found for code {}, session will be closed", packet.code);
            channel.close();
            return;
        }

        submitTask(channel, handler, packet);
    }

    /**
     * 提交任务。如果session已经关联了{@code Sequence}则提交到{@code Sequence} 排队，否则直接提交给线程池。
     * 
     * @param channel
     * @param handler
     * @param packet
     */
    private void submitTask(Channel channel, Handler handler, Packet packet) {
        Runnable task = new RunnableTask(handler, channel, packet);
        Sequence sequence = channel.attr(SEQUENCE_KEY).get();
        if (sequence != null) {
            sequence.addTask(task);
        } else {
            executor.submit(task);
        }
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
        Handler oldHandler = handlerMap.put(Short.valueOf(code), handler);
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
        requestParamType.put(Short.valueOf(code), param);
    }

    /**
     * 获取请求参数类型
     * 
     * @param code
     * @return
     */
    public GeneratedMessage getParamType(short code) {
        return requestParamType.get(Short.valueOf(code));
    }

    /**
     * 生成新消息队列
     * 
     * @return
     */
    public Sequence newSequence() {
        return new Sequence(executor);
    }

    @Override
    public void start() throws Exception {
        loadHandler(Config.getString("HANDLER_SCAN_PACKAGES"));
        LOG.debug("DispatcherHandler start");
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
                    RequestHandler annotation = handler.getAnnotation(RequestHandler.class);
                    if (annotation != null) {
                        short code = annotation.code();
                        Handler handlerInstance = (Handler) handler.newInstance();
                        addHandler(code, handlerInstance);
                        Class<? extends GeneratedMessage> paramType = annotation.requestParamType();
                        GeneratedMessage paramInstance = instantiate(paramType);
                        addParamType(code, paramInstance);
                    }
                }
            }
        }
    }

    // 每个生成的PB协议类都应该有一个getDefaultInstance静态方法
    private GeneratedMessage instantiate(Class<? extends GeneratedMessage> type)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = type.getMethod("getDefaultInstance");
        return (GeneratedMessage) method.invoke(type);
    }

    @Override
    public void stop() {
        BaseUtil.shutdownThreadPool(executor, 5 * 1000);
        LOG.debug("DispatcherHandler stop");
    }

    static class RunnableTask implements Runnable
    {
        private static final Logger LOG = LoggerFactory.getLogger(RunnableTask.class);
        private Handler handler; // 消息处理器
        private Channel channel; // 网络连接
        private Packet packet; // 消息包

        /**
         * @param handler 处理器
         * @param session 网络会话
         * @param packet 消息包
         */
        public RunnableTask(Handler handler, Channel channel, Packet packet) {
            this.handler = handler;
            this.channel = channel;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                handler.handle(channel, packet);
            } catch (Throwable t) {
                LOG.error("{}", toString(), t);
            }
        }

        @Override
        public String toString() {
            return "RunnableTask: [channel=" + channel + ", packet=" + packet + "]";
        }

        public String dumpPacketBody() {
            return Arrays.toString(packet.body);
        }
    }
}
