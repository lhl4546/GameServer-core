/**
 * 
 */
package core.fire.net.tcp;

import java.net.SocketAddress;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 基于Netty实现的网络服务器
 * 
 * @author lhl
 *
 * 2016年1月29日 下午6:16:21
 */
public class TcpServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossgroup;
    private EventLoopGroup childgroup;
    private Channel serverSocket;
    private ServerChannelInitializer channelInitializer;
    private TcpDispatcher dispatcher;
    private SocketAddress address;

    public TcpServer() {
        this.bootstrap = new ServerBootstrap();
        this.bossgroup = new NioEventLoopGroup(1, new DefaultThreadFactory("tcp-acceptor"));
        this.childgroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("tcp-io"));
    }

    /**
     * 设置tcp服务器地址
     * 
     * @param address
     */
    public void setAddress(SocketAddress address) {
        this.address = Objects.requireNonNull(address);
    }

    /**
     * 设置tcp请求分发器和协议编解码器
     * 
     * @param dispatcher
     * @param codecFactory
     */
    public void setDispatcherAndCodec(TcpDispatcher dispatcher, CodecFactory codecFactory) {
        this.dispatcher = dispatcher;
        ServerChannelInitializer initializer = new ServerChannelInitializer();
        initializer.setServerHandler(new ServerHandler(dispatcher));
        initializer.setCodecFactory(codecFactory);
        this.channelInitializer = initializer;
    }

    @Override
    public void start() throws Exception {
        dispatcher.start();
        // @formatter:off
        bootstrap.group(bossgroup, childgroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(channelInitializer)
        .option(ChannelOption.SO_BACKLOG, Integer.valueOf(128))
        .childOption(ChannelOption.SO_LINGER, Integer.valueOf(0))
        .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
        // @formatter:on
        ChannelFuture future = bootstrap.bind(address);
        serverSocket = future.sync().channel();
        LOG.debug("tcp server start listen on {}", address);
    }

    @Override
    public void stop() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (bossgroup != null) {
            bossgroup.shutdownGracefully();
        }
        if (childgroup != null) {
            childgroup.shutdownGracefully();
        }
        dispatcher.stop();
        LOG.debug("tcp server stop");
    }
}
