/**
 * 
 */
package core.fire.executor;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * 消息队列，保证同时只会有一条队列中的消息正在执行
 * 
 * @author lhl
 *
 *         2016年1月30日 下午12:48:21
 */
public final class Sequence
{
    private Queue<Runnable> queue;
    private ExecutorService executor;

    /**
     * @param executor 消息执行器
     * @throws NullPointerException 参数{@code executor}为null
     */
    public Sequence(ExecutorService executor) throws NullPointerException {
        this.executor = Objects.requireNonNull(executor);
        this.queue = new LinkedList<>();
    }

    public final void enqueue(Runnable r) {
        synchronized (queue) {
            queue.offer(r);
            if (queue.size() == 1) {
                executor.submit(r);
            }
        }
    }

    public final void dequeue() {
        synchronized (queue) {
            queue.poll();
            if (!queue.isEmpty()) {
                executor.submit(queue.peek());
            }
        }
    }

    public final int size() {
        synchronized (this.queue) {
            return queue.size();
        }
    }
}
