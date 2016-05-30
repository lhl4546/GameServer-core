package core.fire.net.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import core.fire.Component;
import core.fire.CoreConfiguration;
import core.fire.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 基于Netty实现的Http服务器
 * 
 * @author lhl
 *
 *         2016年3月28日 下午3:48:06
 */
@org.springframework.stereotype.Component
public class HttpServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossgroup;
    private EventLoopGroup childgroup;
    private Channel serverSocket;

    @Autowired
    private HttpServerInitializer initializer;
    @Autowired
    private CoreConfiguration config;

    public HttpServer() {
        this.bossgroup = new NioEventLoopGroup(1, new NamedThreadFactory("http-acceptor"));
        int netioThreads = Runtime.getRuntime().availableProcessors();
        this.childgroup = new NioEventLoopGroup(netioThreads, new NamedThreadFactory("http"));
        this.bootstrap = new ServerBootstrap();
    }

    @Override
    public void start() throws Exception {
        int port = config.getHttpPort();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(bossgroup, childgroup).channel(NioServerSocketChannel.class).childHandler(initializer).childOption(ChannelOption.SO_LINGER, 0).childOption(ChannelOption.TCP_NODELAY, true);
        serverSocket = bootstrap.bind(port).sync().channel();
        LOG.debug("Http server start listen on port {}", port);
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
        LOG.debug("Http server stop");
    }
}
