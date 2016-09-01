package core.fire.net.http;

import java.net.SocketAddress;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 一个基于Netty实现的简易Http服务器，支持GET/POST。POST的body最大长度不要超过1MB
 * 
 * @author lhl
 *
 * 2016年3月28日 下午3:48:06
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
    private SocketAddress address;

    public HttpServer() {
        this.bossgroup = new NioEventLoopGroup(1, new DefaultThreadFactory("http-acceptor"));
        this.childgroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("http-io"));
        this.bootstrap = new ServerBootstrap();
    }

    /**
     * 设置http服务地址
     * 
     * @param address
     */
    public void setAddress(SocketAddress address) {
        this.address = Objects.requireNonNull(address);
    }

    /**
     * 设置http请求分发器
     * 
     * @param dispatcher
     */
    public void setDispatcher(HttpDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher);
        HttpServerInitializer initializer = new HttpServerInitializer(this.dispatcher);
        this.initializer = initializer;
    }

    @Override
    public void start() throws Exception {
        dispatcher.start();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(bossgroup, childgroup).channel(NioServerSocketChannel.class).childHandler(initializer).childOption(ChannelOption.SO_LINGER, 0).childOption(ChannelOption.TCP_NODELAY, true);
        serverSocket = bootstrap.bind(address).sync().channel();
        LOG.debug("http server start listen on {}", address);
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
        LOG.debug("http server stop");
    }
}
