package core.fire;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

import core.fire.executor.Sequence;
import core.fire.net.tcp.CodecFactory;

public class CoreServer
{
    /**
     * @return TCP业务执行线程池
     */
    public ExecutorService getExecutor() {
        throw new AbstractMethodError();
    };

    /**
     * @return TCP协议编解码
     */
    public CodecFactory getCodecFactory() {
        throw new AbstractMethodError();
    };

    /**
     * @return TCp协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getTcpHandlerScanPath() {
        throw new AbstractMethodError();
    };

    /**
     * @return TCP拦截器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getTcpInterceptorScanPath() {
        throw new AbstractMethodError();
    };

    /**
     * @return TCP监听地址
     */
    public SocketAddress getTcpAddress() {
        throw new AbstractMethodError();
    };

    /**
     * @return HTTP监听地址
     */
    public SocketAddress getHttpAddress() {
        throw new AbstractMethodError();
    };

    /**
     * @return HTTP协议处理器扫描路径，支持以英文逗号分隔的多个路径(如 "a.b,c.d")
     */
    public String getHttpHandlerScanPath() {
        throw new AbstractMethodError();
    };

    /**
     * @return HTTP业务执行线程池，返回null表示在HTTP IO线程中执行业务逻辑
     */
    public ExecutorService getHttpExecutor() {
        return null;
    };

    /**
     * @return 一个新任务队列
     */
    public Sequence newSequence() {
        return new Sequence(getExecutor());
    }
}
