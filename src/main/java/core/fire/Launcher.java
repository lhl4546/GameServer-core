package core.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.executor.TcpDispatcher;
import core.fire.net.http.HttpInboundHandler;
import core.fire.net.http.HttpServer;
import core.fire.net.http.HttpDispatcher;
import core.fire.net.http.HttpServerInitializer;
import core.fire.net.tcp.CodecFactory;
import core.fire.net.tcp.NettyChannelInitializer;
import core.fire.net.tcp.NettyHandler;
import core.fire.net.tcp.TcpServer;
import core.fire.net.tcp.PlainProtocolDecoder;
import core.fire.net.tcp.PlainProtocolEncoder;
import core.fire.rpc.RPCServer;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

/**
 * 应用启动器
 * <p>
 * 如需使用数据库访问，需要提供JDBCTemplate Spring bean和DBService Spring bean
 * 
 * @author lhl
 *
 *         2016年5月30日 上午9:11:54
 */
public class Launcher implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    // *****************************************//
    protected CoreConfiguration config;

    protected Timer timer;
    protected TcpDispatcher tcpDispatcher;
    protected TcpServer tcpServer;
    protected HttpServer httpServer;
    protected HttpDispatcher httpDispatcher;
    protected RPCServer rpcServer;
    // *****************************************//

    protected Launcher(CoreConfiguration config) {
        this.config = config;
        initializeComponent();
    }

    protected void initializeComponent() {
        timer = new Timer();

        tcpDispatcher = new TcpDispatcher(config);
        NettyHandler netHandler = new NettyHandler(tcpDispatcher);
        CodecFactory codecFactory = new CodecFactory() {
            PlainProtocolEncoder encoder = new PlainProtocolEncoder();

            @Override
            public ChannelOutboundHandler getEncoder() {
                return encoder;
            }

            @Override
            public ChannelInboundHandler getDecoder() {
                return new PlainProtocolDecoder();
            }
        };
        NettyChannelInitializer channelInitializer = new NettyChannelInitializer(netHandler, codecFactory);
        tcpServer = new TcpServer(channelInitializer, config);

        httpDispatcher = new HttpDispatcher(config);
        HttpInboundHandler httpHandler = new HttpInboundHandler(httpDispatcher);
        HttpServerInitializer httpInitializer = new HttpServerInitializer(httpHandler);
        httpServer = new HttpServer(httpInitializer, config);

        rpcServer = new RPCServer(config);
    }

    @Override
    public final void start() {
        try {
            registerShutdownHook();
            doStart();
        } catch (Exception e) {
            LOG.error("Server start failed", e);
            System.exit(-1);
        }
    }

    /**
     * 注册系统关闭回调
     */
    private void registerShutdownHook() {
        Thread thread = new Thread(() -> stop(), "SHUTDOWN_HOOK");
        Runtime.getRuntime().addShutdownHook(thread);
        LOG.debug("Register shutdown hook");
    }

    /**
     * 启动组件，子类重写该方法以启动自定义组件。本类默认不启动任何组件。
     * <p>
     * core提供组件如下:
     * <ul>
     * <li>Timer</li>
     * <li>DispatcherHandler</li>
     * <li>NettyServer</li>
     * <li>HttpServer</li>
     * <li>HttpServerDispatcher</li>
     * <li>RPCServer</li>
     * </ul>
     * 
     * @throws Exception
     */
    protected void doStart() throws Exception {
    }

    @Override
    public final void stop() {
        try {
            doStop();
        } catch (Exception e) {
            LOG.error("Server stop failed", e);
        }
    }

    /**
     * 停止组件，子类重写该方法以停止自定义组件
     * 
     * @throws Exception
     */
    protected void doStop() throws Exception {
    }
}
