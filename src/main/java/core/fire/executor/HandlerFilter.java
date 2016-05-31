package core.fire.executor;

public interface HandlerFilter
{
    /**
     * 在协议被处理前实施过滤
     * 
     * @param request
     * @param response
     * @return 返回true表示过滤完成，可以继续下个操作，返回false表明过滤完成，但不能继续处理该请求
     */
    boolean doFilter(SocketRequest request, SocketResponse response);
}
