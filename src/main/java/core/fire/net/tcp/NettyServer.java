/**
 * 
 */
package core.fire.net.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import core.fire.Component;
import core.fire.Config;
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
@org.springframework.stereotype.Component
public class NettyServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossgroup;
    private EventLoopGroup childgroup;
    private Channel serverSocket;
    private int port;

    @Autowired
    private NettyChannelInitializer channelInitializer;

    /**
     * @param dispatcher 消息派发处理器
     */
    public NettyServer() {
        this.port = Config.getInt("TCP_PORT");
        this.bootstrap = new ServerBootstrap();
        this.bossgroup = new NioEventLoopGroup(1, new NamedThreadFactory("ACCEPTOR"));
        int netiothreads = Runtime.getRuntime().availableProcessors();
        this.childgroup = new NioEventLoopGroup(netiothreads, new NamedThreadFactory("NET_IO"));
    }

    @Override
    public void start() throws Exception {
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
        LOG.debug("NettyServer stop");
    }
}
