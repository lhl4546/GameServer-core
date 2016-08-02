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
    private final Runnable TERMINATE_TASK = () -> {
    };

    public SingleThreadExecutor(String threadName) {
        Runnable worker = () -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    if (task != null) {
                        if (task == TERMINATE_TASK) {
                            break;
                        }
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

    public void shutdown() {
        taskQueue.add(TERMINATE_TASK);
    }
}