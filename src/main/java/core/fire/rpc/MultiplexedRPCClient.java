/**
 * 
 */
package core.fire.rpc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.rpc.pool.ConnectionManager;

/**
 * 多路复用RPC客户端，适合多路复用RPC服务器
 * <p>
 * 为保证调用顺序，内部维护了一个队列，所有外部RPC调用均被提交到该队列 ，另有一单独线程专门从队列中提取调用并执行
 * 
 * @author lhl
 *
 *         2016年2月25日 下午2:34:43
 */
@org.springframework.stereotype.Component
public class MultiplexedRPCClient implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(MultiplexedRPCClient.class);
    private static final ConnectionManager connectionMgr = new ConnectionManager();
    private static final WorkerThread workerThread = new WorkerThread();

    @Override
    public void start() throws Exception {
        workerThread.startThread();
        LOG.debug("MultiplexedRPCClient start");
    }

    @Override
    public void stop() throws Exception {
        workerThread.stop = true;
        workerThread.thread.join();
        LOG.debug("MultiplexedRPCClient stop");
    }

    /**
     * 异步调用
     * 
     * @param action
     * @param serviceName 服务名
     */
    public void asyncCall(RPCAction action, String serviceName) {
        Runnable task = () -> call0(action, serviceName);
        addTask(task);
    }

    /**
     * 提交任务
     * 
     * @param task
     */
    protected void addTask(Runnable task) {
        workerThread.addTask(task);
    }

    /**
     * 执行RPC调用逻辑
     * 
     * @param action
     * @param serviceName 服务名
     */
    private void call0(RPCAction action, String serviceName) {
        TSocket transport = connectionMgr.getConnection();
        try {
            TProtocol protocol = new TCompactProtocol(transport);
            TMultiplexedProtocol tp = new TMultiplexedProtocol(protocol, serviceName);
            action.action(tp);
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            connectionMgr.returnConnection(transport);
        }
    }

    private static class WorkerThread implements Runnable
    {
        BlockingQueue<Runnable> taskQueue;
        Thread thread;
        boolean stop;

        WorkerThread() {
            this.taskQueue = new LinkedBlockingQueue<>();
            this.thread = new Thread(this, "RPC_CLIENT");
        }

        void startThread() {
            thread.start();
        }

        void addTask(Runnable task) {
            taskQueue.add(task);
        }

        Runnable takeTask() {
            Runnable task = null;
            while (task == null && !stop) {
                try {
                    task = taskQueue.take();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            return task;
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
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }
}
