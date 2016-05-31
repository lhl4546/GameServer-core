package core.fire.executor;

public interface HandlerInterceptor
{
    /**
     * 在协议被处理前实施拦截
     * 
     * @param request
     * @param response
     * @return 返回true表示拦截完成，可以继续下个操作，返回false表明拦截完成，但不能继续处理该请求
     */
    boolean preHandle(SocketRequest request, SocketResponse response);
}
