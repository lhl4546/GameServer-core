package core.fire.net.http;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpServerHandler extends ChannelInboundHandlerAdapter
{
    @Autowired
    private HttpServerDispatcher dispatcher;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            process(ctx.channel(), req);
        }
    }

    public void process(Channel ch, HttpRequest request) throws URISyntaxException {
        URI uri = new URI(request.getUri());
        System.out.println(uri.getPath());
        QueryStringDecoder queryParser = new QueryStringDecoder(uri);
        dispatcher.handle(ch, uri.getPath(), queryParser.parameters());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
