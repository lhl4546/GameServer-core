/**
 * 
 */
package core.fire.timer;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import core.fire.executor.NamedThreadFactory;
import core.fire.util.TimeUtil;
import core.fire.util.Util;

/**
 * 定时器
 * 
 * <p>
 * 定时器不适合执行耗时任务，建议仅用于触发
 * <p>
 * 对于周期性定时任务，如果某个任务执行时间比执行间隔要长，则下次任务将在此次结束后立即执行，不会并发执行。
 * 如果某个周期循环任务抛出异常将导致后续循环无法执行，因此建议任务提交者自行捕获异常避免影响后续执行。
 * 
 * @author lhl
 *
 */
public enum Timer {
    INSTANCE;

    /** 定时任务执行器 */
    private ScheduledExecutorService executor;

    private Timer() {
        executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("timer"));
        ((ScheduledThreadPoolExecutor) executor).setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    /**
     * 立即停止定时器线程，所有等待中的任务将被取消，所有正在执行的任务将被中断
     */
    public void shutdown() {
        Util.shutdownThreadPool(executor, 0);
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
    public ScheduledFuture<?> scheduleEveryWeek(Runnable task, int dayOfWeek, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        long firstDelay = TimeUtil.getWeekDelay(dayOfWeek, hourOfDay, minuteOfHour, secondOfMinute);
        return schedule(task, firstDelay, TimeUtil.MILLIS_PER_WEEK, TimeUnit.MILLISECONDS);
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
        return schedule(task, firstDelay, TimeUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
    }
}
