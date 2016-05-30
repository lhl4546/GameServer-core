/**
 * 
 */
package core.fire;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import core.fire.util.Util;
import core.fire.util.TimeUtil;

/**
 * 定时器
 * 
 * <p>
 * 定时器不适合执行耗时任务，建议仅用于触发
 * 
 * @author lhl
 *
 */
@org.springframework.stereotype.Component
public class Timer implements Component
{
    /** 定时任务执行器 */
    private ScheduledExecutorService executor;

    public Timer() {
        executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("TIMER"));
    }

    /**
     * 执行一次的任务
     * 
     * @param task 任务
     * @param delay 延时
     * @param unit 时间单位
     * @return {@link ScheduledFuture}
     */
    public ScheduledFuture<?> scheduleOneShot(Runnable task, long delay, TimeUnit unit) {
        return executor.schedule(task, delay, unit);
    }

    /**
     * 以固定频率执行任务
     * 
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable
     *      command, long initialDelay, long period, TimeUnit unit)
     * @param task
     * @param delay 第一次执行延时
     * @param period 执行周期
     * @param unit 执行周期时间单位
     * @return {@link ScheduledFuture}
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(task, delay, period, unit);
    }

    /**
     * 以固定频率，在每周的某一时刻执行任务
     * 
     * @see #scheduleAtFixedRate
     * @see java.util.Calendar#DAY_OF_WEEK
     * @param task 执行任务
     * @param dayOfWeek 周几(参考{@link Calendar#DAY_OF_WEEK})
     * @param hourOfDay 时(24小时制)
     * @param minuteOfHour 分
     * @param secondOfMinute 秒
     * @return {@link ScheduledFuture}
     */
    public ScheduledFuture<?> scheduleEveryWeek(Runnable task, int dayOfWeek, int hourOfDay, int minuteOfHour,
            int secondOfMinute) {
        long firstDelay = TimeUtil.getWeekDelay(dayOfWeek, hourOfDay, minuteOfHour, secondOfMinute);
        return executor.scheduleAtFixedRate(task, firstDelay, TimeUtil.MILLIS_PER_WEEK, TimeUnit.MILLISECONDS);
    }

    /**
     * 以固定频率，在每天的某一时刻执行任务
     * 
     * @param task 执行任务
     * @param hourOfDay 时(24小时制)
     * @param minuteOfHour 分
     * @param secondOfMinute 秒
     * @return {@link ScheduledFuture}
     */
    public ScheduledFuture<?> scheduleEveryDay(Runnable task, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        long firstDelay = TimeUtil.getDayDelay(hourOfDay, minuteOfHour, secondOfMinute);
        return executor.scheduleAtFixedRate(task, firstDelay, TimeUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() throws Exception {
        // 这个参数设置为false，后续任务在shutdown后不会再执行
        ((ScheduledThreadPoolExecutor) executor).setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    @Override
    public void stop() throws Exception {
        Util.shutdownThreadPool(executor, 0);
    }
}
