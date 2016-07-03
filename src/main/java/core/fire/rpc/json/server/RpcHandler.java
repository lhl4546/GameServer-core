package core.fire.rpc.json.server;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import core.fire.net.http.HttpHandler;
import core.fire.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;;

/**
 * RPC处理器基类，实现了基本的请求参数转换以及应答数据发送方法
 * 
 * @author lihuoliang
 *
 * @param <T> 请求参数类型
 */
public abstract class RpcHandler<T> implements HttpHandler
{
    private Channel channel;

    @Override
    public void handle(Channel channel, Map<String, List<String>> parameter) {
        List<String> params = parameter.get("methodparams");
        if (!Util.isNullOrEmpty(params)) {
            String theOnlyParam = params.get(0);
            T paramObject = null;
            try {
                paramObject = JSON.parseObject(theOnlyParam, getParamProtoType());
            } catch (Exception e) {
                throw new RuntimeException("Can not parse " + theOnlyParam + " to " + getParamProtoType());
            }
            this.channel = channel;
            handle(paramObject);
        }
    }

    /**
     * 以text/plain格式发送应答信息。发送完毕后将会关闭连接
     * 
     * @param message 应答数据
     */
    protected void sendError(String message) {
        ByteBuf buf = Unpooled.copiedBuffer(message.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        channel.writeAndFlush(response).addListener(CLOSE);
    }

    /**
     * @return 参数原型
     */
    protected abstract Class<T> getParamProtoType();

    /**
     * 处理请求
     * 
     * @param param
     */
    protected abstract void handle(T param);
}
