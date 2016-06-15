package core.fire.executor;

import org.springframework.core.Ordered;

/**
 * TCP协议拦截器接口，实现此接口的拦截器将自动被{@linkplain DispatcherHandler}扫描并注册
 * 
 * @author lhl
 *
 *         2016年6月15日 上午9:28:46
 */
public interface HandlerInterceptor extends Ordered
{
    /**
     * 在协议被处理前实施拦截
     * 
     * @param request
     * @param response
     * @return 返回true表示拦截完成，可以继续下个操作，返回false表明拦截完成，但不能继续处理该请求
     */
    boolean preHandle(SocketRequest request, SocketResponse response);

    @Override
    default int getOrder() {
        return 0;
    }
}
