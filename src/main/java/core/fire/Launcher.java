package core.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.executor.TcpDispatcher;
import core.fire.net.http.HttpDispatcher;
import core.fire.net.http.HttpServer;
import core.fire.net.tcp.CodecFactory;
import core.fire.net.tcp.NettyChannelInitializer;
import core.fire.net.tcp.NettyHandler;
import core.fire.net.tcp.PlainProtocolDecoder;
import core.fire.net.tcp.PlainProtocolEncoder;
import core.fire.net.tcp.TcpServer;
import core.fire.rpc.RPCServer;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

/**
 * 应用启动器，主类可以继承此类并重写需要的方法以实现自定义功能，但是{@code setConfig}方法一定要在{@code start}前面调用
 * 
 * @author lhl
 *
 *         2016年5月30日 上午9:11:54
 */
public class Launcher implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    // ---------------------------------//
    protected CoreConfiguration config;

    protected Timer timer;
    protected TcpDispatcher tcpDispatcher;
    protected TcpServer tcpServer;
    protected HttpServer httpServer;
    protected HttpDispatcher httpDispatcher;
    protected RPCServer rpcServer;
    // ---------------------------------//

    /**
     * 设置配置类
     * 
     * @param config
     */
    protected void setConfig(CoreConfiguration config) {
        this.config = config;
    }

    private void initializeComponent() {
        timer = new Timer();

        tcpDispatcher = new TcpDispatcher(config);
        NettyHandler netHandler = new NettyHandler(tcpDispatcher);
        CodecFactory codecFactory = getCodecFactory();
        NettyChannelInitializer channelInitializer = new NettyChannelInitializer(netHandler, codecFactory);
        tcpServer = new TcpServer(channelInitializer, config);

        httpDispatcher = new HttpDispatcher(config);
        httpServer = new HttpServer(httpDispatcher, config);

        rpcServer = new RPCServer(config);
    }

    /**
     * 子类可以重写该方法以提供自定义的协议编解码
     * 
     * @return 协议编解码器
     */
    protected CodecFactory getCodecFactory() {
        return new CodecFactory() {
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
    }

    @Override
    public final void start() {
        try {
            registerShutdownHook();
            initializeComponent();
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
