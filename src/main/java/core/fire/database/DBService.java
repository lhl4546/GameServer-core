/**
 * 
 */
package core.fire.database;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;

/**
 * @author lhl
 *
 *         2016年2月16日 下午3:45:01
 */
@org.springframework.stereotype.Component
public class DBService implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(DBService.class);
    private WorkerThread worker;

    public DBService() {
        this.worker = new WorkerThread();
    }

    public void addTask(Runnable task) {
        worker.addTask(task);
    }

    @Override
    public void start() throws Exception {
        startWorkerThread();
        LOG.debug("DBService start");
    }

    private void startWorkerThread() {
        worker.startThread();
    }

    @Override
    public void stop() throws Exception {
        worker.stop = true;
        worker.thread.interrupt();
        worker.thread.join();
        LOG.debug("DBService stop");
    }

    static class WorkerThread implements Runnable
    {
        BlockingQueue<Runnable> taskQueue;
        Thread thread;
        boolean stop;

        WorkerThread() {
            this.taskQueue = new LinkedBlockingQueue<>();
            this.thread = new Thread(this, "DB");
        }

        void startThread() {
            thread.start();
        }

        void addTask(Runnable task) {
            this.taskQueue.add(task);
        }

        @Override
        public void run() {
            while (!stop) {
                doTask();
            }

            LOG.debug("Receive stop command, remaining task count {}", taskQueue.size());
            while (!taskQueue.isEmpty()) {
                doTask();
            }
            LOG.debug("Finish all remaining task");
        }

        void doTask() {
            try {
                Runnable task = takeTask();
                if (task != null) {
                    task.run();
                }
            } catch (Throwable t) {
                LOG.error("", t);
            }
        }

        Runnable takeTask() {
            Runnable task = null;
            try {
                task = taskQueue.take();
            } catch (InterruptedException e) {
                // Ignore
            }
            return task;
        }
    }
}
