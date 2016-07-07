package core.fire.executor;

import com.google.protobuf.GeneratedMessage;

import core.fire.executor.HandlerInterceptor;
import core.fire.executor.SocketRequest;
import core.fire.net.tcp.Packet;

/**
 * 参数类型拦截器
 * 
 * @author lhl
 *
 *         2016年5月31日 上午10:06:42
 */
public class ParameterTypeInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(SocketRequest request) {
        Packet packet = request.getPacket();
        if (packet.body != null) {
            GeneratedMessage paramType = request.getPrototype();
            Object requestParameter = packet.toProto(paramType);
            request.setRequestParameter(requestParameter);
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
