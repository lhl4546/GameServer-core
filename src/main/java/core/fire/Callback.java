/**
 * 
 */
package core.fire;

/**
 * 回调接口
 * 
 * @author lhl
 *
 *         2016年2月18日 下午4:00:11
 */
public interface Callback
{
    /**
     * 异步操作成功时调用
     * 
     * @param param 异步操作结果
     */
    void onSuccess(Object param);

    /**
     * 异步操作失败时调用
     * 
     * @param t 异步操作异常
     */
    void onError(Throwable t);
}
