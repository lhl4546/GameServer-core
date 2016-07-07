package core.fire.executor;

/**
 * TCP协议拦截器接口，实现此接口的拦截器将自动被{@linkplain TcpDispatcher}扫描并注册。 该接口继承了
 * {@link java.lang.Comparable}接口以实现对多个拦截器进行排序，排序先后由 {@link #getOrder()}
 * 方法返回值决定，返回值越小排名越靠前，默认返回值为0，具体拦截器实现可以重写{@code getOrder()}方法以调整自身排序
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
     * @return 返回true表示拦截完成，可以继续下个操作，返回false表明拦截完成，但不能继续处理该请求
     */
    boolean preHandle(SocketRequest request);

    /**
     * @return order值越大排名越低，默认值为0
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
