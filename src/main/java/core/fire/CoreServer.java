package core.fire;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import core.fire.eventbus.EventBus;
import core.fire.executor.Sequence;
import core.fire.net.tcp.CodecFactory;

public class CoreServer
{
    /** 事件总线 */
    private EventBus eventBus = new EventBus();

    /**
     * 注册事件处理器
     * 
     * @param object
     */
    public void regisEventHandler(Object object) {
        eventBus.register(object);
    }

    /**
     * 提交事件
     * 
     * @param object
     */
    public void postEvent(Object object) {
        eventBus.post(object);
    }

    /**
     * @return 异步任务线程池
     */
    public ExecutorService getAsyncExecutor() {
        throw new AbstractMethodError();
    }

    /**
     * 执行异步任务
     * <p>
     * 要使用该功能必须先重写{@linkplain #getAsyncExecutor()}方法
     * 
     * @param task
     * @return {@linkplain Future}
     */
    public Future<?> async(Runnable task) {
        return getAsyncExecutor().submit(task);
    }

    /**
     * 执行异步任务，并绑定一个回调
     * <p>
     * 要使用该功能必须先重写{@linkplain #getAsyncExecutor()}方法
     * 
     * @param task
     * @param cb
     */
    public void async(Runnable task, Callback<?> cb) {
        Runnable callbackTask = () -> {
            try {
                task.run();
                cb.onSuccess(null);
            } catch (Throwable t) {
                cb.onError(t);
            }
        };
        getAsyncExecutor().execute(callbackTask);
    }

    /**
     * @return TCP业务执行线程池
     */
    public ExecutorService getTcpExecutor() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP协议编解码
     */
    public CodecFactory getCodecFactory() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCp协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getTcpHandlerScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP拦截器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getTcpInterceptorScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP监听地址
     */
    public SocketAddress getTcpAddress() {
        throw new AbstractMethodError();
    }

    /**
     * @return HTTP监听地址
     */
    public SocketAddress getHttpAddress() {
        throw new AbstractMethodError();
    }

    /**
     * @return HTTP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getHttpHandlerScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return HTTP业务执行线程池，返回null表示在HTTP IO线程中执行业务逻辑
     */
    public ExecutorService getHttpExecutor() {
        return null;
    }

    /**
     * @return 一个新任务队列
     */
    public Sequence newSequence() {
        return new Sequence(getTcpExecutor());
    }

    /**
     * @return 数值模版扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getTemplateScanPath() {
        throw new AbstractMethodError();
    }
}
