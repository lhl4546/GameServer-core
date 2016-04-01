/**
 * 
 */
package core.fire.net.http;

import java.util.List;
import java.util.Map;

import io.netty.channel.Channel;

/**
 * GET/POST请求处理器。发送应答是子类的责任
 * 
 * @author lhl
 *
 *         2016年3月28日 下午3:51:19
 */
public interface HttpHandler
{
    /**
     * Http请求处理接口(GET/POST)
     * 
     * @param channel
     * @param parameter
     */
    void handle(Channel channel, Map<String, List<String>> parameter);
}
