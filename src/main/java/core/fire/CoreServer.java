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
     * 创建一个tcp服务器，必须重写getTcpExecutor、getTcpHandlerScanPath、getTcpInterceptorScanPath、getCodecFactory、getTcpAddress方法
     * 
     * @param configuration tcp服务器配置
     */
    protected void createTcpServer(TcpServerConfiguration configuration) {
        TcpDispatcher dispatcher = new TcpDispatcher();
        dispatcher.setExecutor(configuration.getTcpExecutor());
        dispatcher.setHandlerScanPath(configuration.getTcpHandlerScanPath());
        dispatcher.setInterceptorScanPath(configuration.getTcpInterceptorScanPath());
        CodecFactory codecFactory = configuration.getCodecFactory();
        TcpServer server = new TcpServer();
        server.setAddress(configuration.getTcpAddress());
        server.setDispatcherAndCodec(dispatcher, codecFactory);
        this.tcpServer = server;
    }

    /**
     * 创建一个http服务器，必须重写getHttpExecutor、getHttpHandlerScanPath、getHttpAddress方法
     */
    protected void createHttpServer(HttpServerConfiguration configuration) {
        HttpDispatcher dispatcher = new HttpDispatcher();
        dispatcher.setExecutor(configuration.getHttpExecutor());
        dispatcher.setHandlerScanPath(configuration.getHttpHandlerScanPath());
        HttpServer server = new HttpServer();
        server.setAddress(configuration.getHttpAddress());
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

    /**
     * tcp服务器配置
     * 
     * @author lhl
     *
     * 2016年9月1日 下午4:42:26
     */
    public interface TcpServerConfiguration
    {
        /**
         * @return TCP业务执行线程池
         */
        ExecutorService getTcpExecutor();

        /**
         * @return TCP协议编解码
         */
        CodecFactory getCodecFactory();

        /**
         * @return TCP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
         */
        String getTcpHandlerScanPath();

        /**
         * @return TCP拦截器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
         */
        String getTcpInterceptorScanPath();

        /**
         * @return TCP监听地址
         */
        SocketAddress getTcpAddress();
    }

    /**
     * http服务器配置
     * 
     * @author lhl
     *
     * 2016年9月1日 下午4:43:07
     */
    public interface HttpServerConfiguration
    {
        /**
         * @return HTTP监听地址
         */
        SocketAddress getHttpAddress();

        /**
         * @return HTTP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
         */
        String getHttpHandlerScanPath();

        /**
         * @return HTTP业务执行线程池，返回null表示在HTTP IO线程中执行业务逻辑，默认返回null
         */
        default ExecutorService getHttpExecutor() {
            return null;
        }
    }
}
