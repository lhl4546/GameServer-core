package core.fire;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import core.fire.eventbus.EventBus;
import core.fire.net.http.HttpDispatcher;
import core.fire.net.http.HttpServer;
import core.fire.net.tcp.CodecFactory;
import core.fire.net.tcp.TcpDispatcher;
import core.fire.net.tcp.TcpServer;

/**
 * @author lhl
 *
 * 2016年8月25日 下午3:19:32
 */
public class CoreServer implements Component
{
    // 事件总线
    private EventBus eventBus = new EventBus();
    // http服务器
    private HttpServer httpServer;
    // tcp服务器
    private TcpServer tcpServer;

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
    protected ExecutorService getTcpExecutor() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP协议编解码
     */
    protected CodecFactory getCodecFactory() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    protected String getTcpHandlerScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP拦截器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    protected String getTcpInterceptorScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return TCP监听地址
     */
    protected SocketAddress getTcpAddress() {
        throw new AbstractMethodError();
    }

    /**
     * 创建一个tcp服务器，必须重写getTcpExecutor、getTcpHandlerScanPath、getTcpInterceptorScanPath、getCodecFactory、getTcpAddress方法
     */
    protected void createTcpServer() {
        TcpDispatcher dispatcher = new TcpDispatcher();
        dispatcher.setExecutor(getTcpExecutor());
        dispatcher.setHandlerScanPath(getTcpHandlerScanPath());
        dispatcher.setInterceptorScanPath(getTcpInterceptorScanPath());
        CodecFactory codecFactory = getCodecFactory();
        TcpServer server = new TcpServer();
        server.setAddress(getTcpAddress());
        server.setDispatcherAndCodec(dispatcher, codecFactory);
        this.tcpServer = server;
    }

    /**
     * @return HTTP监听地址
     */
    protected SocketAddress getHttpAddress() {
        throw new AbstractMethodError();
    }

    /**
     * @return HTTP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    protected String getHttpHandlerScanPath() {
        throw new AbstractMethodError();
    }

    /**
     * @return HTTP业务执行线程池，返回null表示在HTTP IO线程中执行业务逻辑
     */
    protected ExecutorService getHttpExecutor() {
        return null;
    }

    /**
     * 创建一个http服务器，必须重写getHttpExecutor、getHttpHandlerScanPath、getHttpAddress方法
     */
    protected void createHttpServer() {
        HttpDispatcher dispatcher = new HttpDispatcher();
        dispatcher.setExecutor(getHttpExecutor());
        dispatcher.setHandlerScanPath(getHttpHandlerScanPath());
        HttpServer server = new HttpServer();
        server.setAddress(getHttpAddress());
        server.setDispatcher(dispatcher);
        this.httpServer = server;
    }

    @Override
    final public void start() throws Exception {
        beforeStart();
        start0();
        afterStart();
    }

    /**
     * 启动tcp服务器、http服务器
     * 
     * @throws Exception
     */
    private void start0() throws Exception {
        if (tcpServer != null) {
            tcpServer.start();
        }
        if (httpServer != null) {
            httpServer.start();
        }
    }

    /**
     * 启动前回调
     */
    protected void beforeStart() {
    }

    /**
     * 启动后回调
     */
    protected void afterStart() {
    }

    @Override
    final public void stop() throws Exception {
        beforeStop();
        stop0();
        afterStop();
    }

    /**
     * 停止tcp服务器、http服务器
     * 
     * @throws Exception
     */
    private void stop0() throws Exception {
        if (tcpServer != null) {
            tcpServer.stop();
        }
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    /**
     * 停止前回调
     */
    protected void beforeStop() {
    }

    /**
     * 停止后回调
     */
    protected void afterStop() {
    }
}
