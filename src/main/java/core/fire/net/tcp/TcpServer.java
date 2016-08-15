/**
 * 
 */
package core.fire.net.tcp;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.CoreServer;
import core.fire.NamedThreadFactory;
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
    private ServerChannelInitializer channelInitializer;
    private TcpDispatcher dispatcher;
    private SocketAddress address;

    public TcpServer(CoreServer core) {
        this.dispatcher = new TcpDispatcher(core);
        this.channelInitializer = new ServerChannelInitializer(dispatcher, core.getCodecFactory());
        this.address = core.getTcpAddress();
        this.bootstrap = new ServerBootstrap();
        this.bossgroup = new NioEventLoopGroup(1, new NamedThreadFactory("acceptor"));
        int netiothreads = Runtime.getRuntime().availableProcessors();
        this.childgroup = new NioEventLoopGroup(netiothreads, new NamedThreadFactory("socket"));
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
        LOG.debug("NettyServer start listen on {}", address);
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
