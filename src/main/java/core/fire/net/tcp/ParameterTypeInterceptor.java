package core.fire.net.tcp;

import com.google.protobuf.GeneratedMessage;

/**
 * 参数类型拦截器
 * 
 * @author lhl
 *
 *         2016年5月31日 上午10:06:42
 */
public class ParameterTypeInterceptor implements HandlerInterceptor
{
    private TcpDispatcher dispatcher;
    
    public ParameterTypeInterceptor(TcpDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public boolean preHandle(SocketRequest request) {
        IPacket packet = request.getPacket();
        if (packet.getBody() != null) {
            GeneratedMessage paramType = dispatcher.getParamType(packet.getCode());
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
