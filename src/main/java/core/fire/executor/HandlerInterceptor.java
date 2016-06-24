package core.fire.executor;

/**
 * TCP协议拦截器接口，实现此接口的拦截器将自动被{@linkplain TcpDispatcher}扫描并注册
 * 
 * @author lhl
 *
 *         2016年6月15日 上午9:28:46
 */
public interface HandlerInterceptor extends Comparable<HandlerInterceptor>
{
    /**
     * 在协议被处理前实施拦截
     * 
     * @param request
     * @param response
     * @return 返回true表示拦截完成，可以继续下个操作，返回false表明拦截完成，但不能继续处理该请求
     */
    boolean preHandle(SocketRequest request, SocketResponse response);

    /**
     * @return order值越大排名越低
     */
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(HandlerInterceptor o) {
        int thisOrder = getOrder();
        int thatOrder = o.getOrder();
        int diff = thisOrder - thatOrder;
        return diff < 0 ? 1 : (diff == 0 ? 0 : -1);
    }
}
