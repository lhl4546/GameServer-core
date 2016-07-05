package core.fire.rpc.json.server;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AttributeKey;;

/**
 * RPC处理器基类，实现了基本的请求参数转换以及应答数据发送方法
 * 
 * @author lihuoliang
 *
 * @param <T> 请求参数类型
 */
public abstract class RpcHandler<T>
{
    static final AttributeKey<Long> ID = AttributeKey.valueOf("ID");

    public void handle(Channel channel, RpcRequest request) {
        String methodParam = request.getMethodParams();
        T paramObject = null;
        try {
            paramObject = JSON.parseObject(methodParam, getParamProtoType());
        } catch (Exception e) {
            throw new RuntimeException("Can not parse " + methodParam + " to " + getParamProtoType());
        }
        channel.attr(ID).set(request.getId());
        handle(channel, paramObject);
    }

    /**
     * 以text/plain格式发送应答信息。发送完毕后将会关闭连接
     * 
     * @param channel
     * @param message 应答数据
     */
    protected void sendResponse(Channel channel, String message) {
        RpcResponse response = new RpcResponse();
        response.setId(channel.attr(ID).get());
        response.setData(message);
        ByteBuf buf = Unpooled.copiedBuffer(response.toString().getBytes());
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);
        httpResponse.headers().set(CONTENT_TYPE, "text/plain");
        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
        channel.writeAndFlush(httpResponse).addListener(CLOSE);
    }

    /**
     * @return 参数原型
     */
    protected abstract Class<T> getParamProtoType();

    /**
     * 处理请求
     * 
     * @param channel
     * @param param
     */
    protected abstract void handle(Channel channel, T param);
}
