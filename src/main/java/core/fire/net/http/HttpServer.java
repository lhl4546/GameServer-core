/**
 * 
 */
package core.fire.net.http;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.Config;
import core.fire.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 仅适用于Http GET请求
 * 
 * @author lhl
 *
 *         2016年3月17日 下午3:27:46
 */
@org.springframework.stereotype.Component
public class HttpServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);
    private EventLoopGroup acceptor;
    private EventLoopGroup selector;
    private Channel ch;

    public HttpServer() {
        acceptor = new NioEventLoopGroup(1, new NamedThreadFactory("HTTP_ACCEPT"));
        int netiothreads = Runtime.getRuntime().availableProcessors();
        selector = new NioEventLoopGroup(netiothreads, new NamedThreadFactory("HTTP_IO"));
    }

    @Override
    public void start() throws Exception {
        ServerBootstrap boot = new ServerBootstrap();
        boot.option(ChannelOption.SO_BACKLOG, 1024);
        boot.group(acceptor, selector);
        boot.channel(NioServerSocketChannel.class);
        boot.childHandler(new HttpServerInitializer());
        int port = Config.getInt("HTTP_PORT");
        ch = boot.bind(port).sync().channel();
        LOG.debug("Http server start listen on {}", port);
    }

    @Override
    public void stop() throws Exception {
        if (ch != null) {
            ch.close();
        }
        acceptor.shutdownGracefully(0L, 0L, TimeUnit.SECONDS);
        selector.shutdownGracefully(0L, 0L, TimeUnit.SECONDS);
    }
}
