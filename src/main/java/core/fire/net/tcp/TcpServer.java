/**
 * 
 */
package core.fire.net.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.NamedThreadFactory;
import core.fire.executor.TcpDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 基于Netty实现的网络服务器
 * 
 * @author lhl
 *
 *         2016年1月29日 下午6:16:21
 */
public class TcpServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossgroup;
    private EventLoopGroup childgroup;
    private Channel serverSocket;
    private NettyChannelInitializer channelInitializer;
    private TcpDispatcher dispatcher;
    private int port;

    /**
     * @param channelInitializer
     * @param port
     */
    public TcpServer(NettyChannelInitializer channelInitializer, int port) {
        this.dispatcher = channelInitializer.getDispatcher();
        this.channelInitializer = channelInitializer;
        this.port = port;
        this.bootstrap = new ServerBootstrap();
        this.bossgroup = new NioEventLoopGroup(1, new NamedThreadFactory("ACCEPTOR"));
        int netiothreads = Runtime.getRuntime().availableProcessors();
        this.childgroup = new NioEventLoopGroup(netiothreads, new NamedThreadFactory("socket"));
    }

    @Override
    public void start() throws Exception {
        dispatcher.start();
        bootstrap.group(bossgroup, childgroup).channel(NioServerSocketChannel.class).childHandler(channelInitializer).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_LINGER, 0)
                .childOption(ChannelOption.TCP_NODELAY, true);
        ChannelFuture future = bootstrap.bind(port);
        serverSocket = future.sync().channel();
        LOG.debug("NettyServer start listen on port {}", port);
    }

    @Override
    public void stop() {
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
        LOG.debug("NettyServer stop");
    }
}
