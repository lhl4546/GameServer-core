/**
 * 
 */
package core.fire;

import core.fire.executor.Sequence;

/**
 * 为保证回调逻辑在指定消息队列执行，需要使用{@code AsyncCallback}
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
