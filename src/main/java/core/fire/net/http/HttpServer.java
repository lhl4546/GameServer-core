package core.fire.net.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 一个基于Netty实现的简易Http服务器，支持GET/POST。
 * 
 * @author lhl
 *
 *         2016年3月28日 下午3:48:06
 */
public class HttpServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossgroup;
    private EventLoopGroup childgroup;
    private Channel serverSocket;
    private HttpServerInitializer initializer;
    private HttpDispatcher dispatcher;
    private int port;

    public HttpServer(HttpDispatcher dispatcher, int port) {
        this.dispatcher = dispatcher;
        HttpServerInitializer initializer = new HttpServerInitializer(dispatcher);
        this.initializer = initializer;
        this.port = port;
        this.bossgroup = new NioEventLoopGroup(1, new NamedThreadFactory("http-acceptor"));
        int netioThreads = Runtime.getRuntime().availableProcessors();
        this.childgroup = new NioEventLoopGroup(netioThreads, new NamedThreadFactory("http"));
        this.bootstrap = new ServerBootstrap();
    }

    @Override
    public void start() throws Exception {
        dispatcher.start();
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
        dispatcher.stop();
        LOG.debug("Http server stop");
    }
}
