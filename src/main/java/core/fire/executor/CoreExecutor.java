/**
 * 
 */
package core.fire.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import core.fire.util.Util;

/**
 * 带定时统计功能的线程池
 * 
 * @author lihuoliang
 *
 */
public class CoreExecutor extends ScheduledThreadPoolExecutor
{
    // 统计间隔，单位小时
    private int statisticPeriod;

    public CoreExecutor(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory);
        statisticPeriod = 2;
        scheduleStatisticTask();
    }

    protected void scheduleStatisticTask() {
        Runnable task = () -> {
            String info = toString();
            logStatisticInfo(info);
        };
        scheduleAtFixedRate(task, 0, statisticPeriod, TimeUnit.HOURS);
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
        Util.shutdownThreadPool(this, timeToWait);
    }
}
