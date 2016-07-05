/**
 * 
 */
package core.fire.rpc.json.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.JSON;

import core.fire.rpc.json.server.RpcRequest;
import core.fire.util.HttpUtil;

/**
 * @author lihuoliang
 *
 */
public class RpcUtil
{
    private static AtomicLong longId = new AtomicLong(0);

    /**
     * 调用
     * 
     * @param methodName 方法名
     * @param parameter 方法参数
     */
    public static void invoke(String methodName, Object parameter) {
        RpcRequest request = new RpcRequest();
        request.setId(longId.incrementAndGet());
        request.setMethodName(methodName);
        request.setMethodParams(JSON.toJSONString(parameter));
        String httpParam = request.toString();
        httpParam = urlEncode(httpParam);
        String resp = HttpUtil.GET("http://localhost/rpc?request=" + httpParam);
        System.out.println(resp);
    }

    private static String urlEncode(String source) {
        try {
            return URLEncoder.encode(source, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
