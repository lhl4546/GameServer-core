/**
 * 
 */
package core.fire;

import core.fire.executor.Sequence;

/**
 * 异步回调，回调逻辑将提交到指定队列执行。如果无需保证回调逻辑在指定线程执行请使用{@code Callback}
 * 
 * @author lhl
 *
 *         2016年7月11日 下午2:13:27
 */
public abstract class AsyncCallback implements Callback
{
    private Sequence sequence;

    public AsyncCallback(Sequence sequence) {
        this.sequence = sequence;
    }

    final public void onSuccess(Object param) {
        sequence.addTask(() -> doSuccess(param));
    }

    /**
     * 异步操作成功
     * 
     * @param param
     */
    protected abstract void doSuccess(Object param);

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
