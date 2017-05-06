/**
 * 
 */
package core.fire.async;

/**
 * 回调接口，回调逻辑将在执行异步逻辑的线程中执行。如果需要保证回调逻辑在指定线程执行请使用{@code AsyncCallback}
 * 
 * @author lhl
 *
 *         2016年2月18日 下午4:00:11
 */
public interface Callback<T>
{
    /**
     * 操作成功时调用
     * 
     * @param param 操作结果
     */
    void onSuccess(T param);

    /**
     * 操作抛出异常时调用
     * 
     * @param t
     */
    void onError(Throwable t);

    /**
     * 空回调
     */
    Callback<Void> NOOP = new Callback<Void>() {
        @Override
        public void onSuccess(Void param) {
        }

        @Override
        public void onError(Throwable t) {
        }
    };
}
