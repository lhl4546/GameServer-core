/**
 * 
 */
package core.fire.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import core.fire.util.Util;

/**
 * 带定时统计功能的线程池
 * 
 * @author lihuoliang
 *
 */
public class CoreExecutor implements Executor
{
    // 默认统计间隔
    private static final int DEFAULT_STATISTIC_PERIOD = 2;
    // 统计间隔，单位小时
    private int statisticPeriod;
    // 内部执行线程池
    private ScheduledExecutorService executor;

    /**
     * @param threadFactory 线程工厂
     */
    public CoreExecutor(ThreadFactory threadFactory) {
        this(DEFAULT_STATISTIC_PERIOD, threadFactory);
    }

    /**
     * @param statisticPeriod 统计间隔(单位小时)
     * @param threadFactory 线程工厂
     */
    public CoreExecutor(int statisticPeriod, ThreadFactory threadFactory) {
        executor = Executors.newScheduledThreadPool(1, threadFactory);
        this.statisticPeriod = statisticPeriod;
        scheduleStatisticTask();
    }

    // 启动定时统计任务
    protected void scheduleStatisticTask() {
        Runnable task = () -> {
            String info = toString();
            logStatisticInfo(info);
        };
        executor.scheduleAtFixedRate(task, 0, statisticPeriod, TimeUnit.HOURS);
    }

    /**
     * 子类重写该方法以记录统计信息到自定义位置
     * 
     * @param info
     */
    protected void logStatisticInfo(String info) {
        System.out.println(info);
    }

    /**
     * 停止线程池
     * 
     * @param timeToWait 停止等待时间，单位毫秒
     */
    public void shutdown(int timeToWait) {
        Util.shutdownThreadPool(executor, timeToWait);
    }

    /**
     * 返回线程池统计信息
     */
    @Override
    public String toString() {
        return executor.toString();
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
