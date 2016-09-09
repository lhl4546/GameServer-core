/**
 *
 */
package core.fire.net.tcp;

import core.fire.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * TCP客户端。异步事件驱动，用户需要自定义协议编解码和注册事件处理器
 *
 * @author lhl
 *         <p>
 *         2015年8月13日上午11:56:48
 */
public class SocketClient
{
    protected Bootstrap bootstrap;
    protected EventLoopGroup multiplexer;
    protected Channel channel;
    protected SocketAddress address;

    private SocketClient() {
    }

    /**
     * 连接远程主机
     *
     * @throws RuntimeException 如果无法建立与远程主机的连接
     */
    public void connect() {
        ChannelFuture cf = bootstrap.connect(address);
        cf.awaitUninterruptibly(5, TimeUnit.SECONDS);
        if (!cf.isSuccess()) {
            throw new RuntimeException("Can not connect to " + address);
        }
        channel = cf.channel();
    }

    /**
     * 当前是否已经建立连接
     *
     * @return true：已经建立连接
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    /**
     * 发送网络数据，每次发送之前都会判断网络连接是否正常
     *
     * @param obj
     * @return
     */
    public ChannelFuture send(Object obj) {
        validateBeforeWrite();
        return channel.writeAndFlush(obj);
    }

    /**
     * 验证当前连接是否有效，若连接已失效则重新建立连接
     */
    private void validateBeforeWrite() {
        if (!isConnected()) {
            connect();
        }
    }

    /**
     * 返回远程主机地址
     *
     * @return
     */
    public String remoteAddress() {
        return address.toString();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isConnected()) {
            channel.close();
        }
    }

    /**
     * 断开当前连接，并释放相关资源
     */
    public void shutdown() {
        close();
        if (multiplexer != null) {
            multiplexer.shutdownGracefully();
        }
    }

    /**
     * SocketClient构建器
     */
    public static class SocketClientBuilder
    {
        SocketAddress address;
        CodecFactory codecFactory;
        ChannelInboundHandler handler;

        private SocketClientBuilder() {
        }

        public static SocketClientBuilder builder() {
            return new SocketClientBuilder();
        }

        /**
         * 设置要连接的远程主机地址
         *
         * @param address
         * @return
         */
        public SocketClientBuilder setHost(SocketAddress address) {
            this.address = address;
            return this;
        }

        /**
         * 设置协议编解码工厂
         *
         * @param codecFactory
         * @return
         */
        public SocketClientBuilder setCodecFactory(CodecFactory codecFactory) {
            this.codecFactory = codecFactory;
            return this;
        }

        /**
         * 设置IO事件处理器
         *
         * @param handler
         * @return
         */
        public SocketClientBuilder setHandler(ChannelInboundHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * 构建SocketClient
         *
         * @return
         */
        public SocketClient build() {
            ChannelInitializer<Channel> initializer = buildInitializer();
            NioEventLoopGroup multiplexer = new NioEventLoopGroup(1, new NamedThreadFactory("client"));
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(multiplexer).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(initializer);
            SocketClient client = new SocketClient();
            client.bootstrap = bootstrap;
            client.multiplexer = multiplexer;
            client.address = address;
            return client;
        }

        private ChannelInitializer<Channel> buildInitializer() {
            return new ChannelInitializer<Channel>()
            {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipe = ch.pipeline();
                    pipe.addLast("ENCODER", codecFactory.getEncoder());
                    pipe.addLast("DECODER", codecFactory.getDecoder());
                    pipe.addLast("HANDLER", handler);
                }
            };
        }
    }
}
