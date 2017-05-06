/**
 * 
 */
package core.fire.async;

import core.fire.executor.Sequence;

/**
 * 异步回调，回调逻辑将提交到指定队列执行。如果无需保证回调逻辑在指定线程执行请使用{@code Callback}
 * 
 * @author lhl
 *
 *         2016年7月11日 下午2:13:27
 */
public abstract class AsyncCallback<T> implements Callback<T>
{
    private Sequence sequence; // 回调将在此队列中顺序执行

    /**
     * @param sequence 回调执行顺序队列
     */
    public AsyncCallback(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    final public void onSuccess(T param) {
        sequence.addTask(() -> doSuccess(param));
    }

    /**
     * 异步操作成功
     * 
     * @param param
     */
    protected abstract void doSuccess(T param);

    @Override
    final public void onError(Throwable t) {
        sequence.addTask(() -> doError(t));
    }

    /**
     * 异步操作异常
     * 
     * @param t
     */
    protected abstract void doError(Throwable t);
}
