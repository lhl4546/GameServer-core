/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package core.fire.net.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.AttributeKey;

/**
 * Http处理器，支持GET\POST(application/x-www-form-urlencoded)
 * 
 * @author lhl
 *
 *         2016年3月28日 下午3:49:19
 */
public class HttpInboundHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpInboundHandler.class);
    public static final AttributeKey<String> KEY_PATH = AttributeKey.valueOf("KEY_PATH");
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(false);
    private HttpPostRequestDecoder decoder;
    private HttpDispatcher dispatcher;

    public HttpInboundHandler(HttpDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            URI uri = URI.create(req.getUri());
            ctx.channel().attr(KEY_PATH).set(uri.getPath());

            if (req.getMethod().equals(HttpMethod.GET)) {
                QueryStringDecoder decoder = new QueryStringDecoder(URI.create(req.getUri()));
                Map<String, List<String>> parameter = decoder.parameters();
                dispatch(ctx.channel(), parameter);
                return;
            }

            decoder = new HttpPostRequestDecoder(factory, req);
        }

        if (decoder != null) {
            if (msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                decoder.offer(chunk);

                List<InterfaceHttpData> list = decoder.getBodyHttpDatas();
                Map<String, List<String>> parameter = new HashMap<>();
                for (InterfaceHttpData data : list) {
                    if (data.getHttpDataType() == HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        addParameter(attribute.getName(), attribute.getValue(), parameter);
                    }
                }

                if (chunk instanceof LastHttpContent) {
                    reset();
                    dispatch(ctx.channel(), parameter);
                }
            }
        }
    }

    private void dispatch(Channel channel, Map<String, List<String>> parameter) {
        dispatcher.handle(channel, parameter);
    }

    private void addParameter(String key, String value, Map<String, List<String>> parameter) {
        List<String> list = parameter.get(key);
        if (list == null) {
            list = new ArrayList<>();
            parameter.put(key, list);
        }
        list.add(value);
    }

    private void reset() {
        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("", cause);
        ctx.close();
    }
}
