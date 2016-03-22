/**
 * 
 */
package core.fire.executor;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * 任务队列，保证任务串行化执行。使用非常简单，实例化之后只需添加任务，无需管理任务的执行
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

    /**
     * 添加任务
     * 
     * @param r
     */
    public final void addTask(Runnable r) {
        Runnable task = wrapTask(r);
        synchronized (queue) {
            queue.offer(task);
            if (queue.size() == 1) {
                executor.submit(task);
            }
        }
    }

    /**
     * 驱动任务执行
     * <p>
     * 从队首删除一个任务，如果队列仍然不为空，则继续从队首取出一个任务提交执行
     */
    private final void drive() {
        synchronized (queue) {
            queue.poll();
            if (!queue.isEmpty()) {
                executor.submit(queue.peek());
            }
        }
    }

    /**
     * 返回当前剩余任务数量，可能包括正在执行的任务
     * 
     * @return
     */
    public final int size() {
        synchronized (this.queue) {
            return queue.size();
        }
    }

    /**
     * 封装任务，以达到驱动队列自动执行的目的
     * 
     * @param task
     * @return
     */
    private Runnable wrapTask(Runnable task) {
        return new TaskWrapper(task);
    }

    private class TaskWrapper implements Runnable
    {
        private Runnable task;

        TaskWrapper(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } finally {
                drive();
            }
        }
    }
}
