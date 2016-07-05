/**
 * 
 */
package core.fire.rpc.json.client;

import java.util.concurrent.atomic.AtomicLong;

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
    public static void invoke(String methodName, String parameter) {
        RpcRequest request = new RpcRequest();
        request.setId(longId.incrementAndGet());
        request.setMethodName(methodName);
        request.setMethodParams(parameter);
        String httpParam = request.toString();
        String resp = HttpUtil.GET("http://localhost/rpc?" + httpParam);
        System.out.println(resp);
    }
}
