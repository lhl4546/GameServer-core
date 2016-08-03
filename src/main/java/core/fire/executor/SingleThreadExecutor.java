package core.fire.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单线程执行器
 * 
 * @author lihuoliang
 *
 */
public class SingleThreadExecutor implements Executor
{
    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadExecutor.class);
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread thread;
    private boolean running = true;
    private final Runnable TERMINATE_TASK = () -> running = false;

    public SingleThreadExecutor(String threadName) {
        startWorker(threadName);
    }

    private void startWorker(String threadName) {
        Runnable worker = () -> {
            while (running) {
                try {
                    Runnable task = taskQueue.take();
                    if (task != null) {
                        task.run();
                    }
                } catch (Throwable t) {
                    LOG.error("", t);
                }
            }
        };
        thread = new Thread(worker, threadName);
        thread.start();
    }

    @Override
    public void execute(Runnable command) {
        taskQueue.offer(command);
    }

    /**
     * 当前队列中处于等待状态的任务数量
     * 
     * @return
     */
    public int waitCount() {
        return taskQueue.size();
    }

    /**
     * 停止线程
     * 
     * @param immediately true表示立即中断当前执行中的任务并停止线程，false表示需要等待当前队列中的任务全部执行完毕
     */
    public void shutdown(boolean immediately) {
        if (immediately) {
            running = false;
            thread.interrupt();
        } else {
            execute(TERMINATE_TASK);
        }
    }
}