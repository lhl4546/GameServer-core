/**
 * 
 */
package core.fire.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author lhl
 *
 */
final public class TimeUtil
{
    private TimeUtil() {
    }

    /** 1分钟的毫秒数 */
    public static final long MILLIS_PER_MINUTE = 60 * 1000;

    /** 1小时的毫秒数 */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    /** 1天的毫秒数 */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    /** 1周的毫秒数 */
    public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

    /**
     * 获取一个在当前时间经过{@code delayInMillis}毫秒之后的时间
     * 
     * @param delayInMillis
     * @return {@link java.util.Date}
     */
    public static Date getFutureDateAfterDelay(long delayInMillis) {
        return new Date(System.currentTimeMillis() + delayInMillis);
    }

    /**
     * 获取距离现在滚动指定天数后的某一天
     * 
     * @param days 滚动的天数，可以是负数，表示过去的某一天，正数表示未来某一天
     * @return 滚动后的某一天
     */
    public static Date dateInRoll(int days) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, days);
        return now.getTime();
    }

    /**
     * 计算2个日期之间隔了多少天
     * <p>
     * 将2个日期均推至各自的0点0分0秒0毫秒，然后得出2个日期的毫秒差，经过换算得到的值即2者天数之差
     * <p>
     * 不能直接使用{@link Calendar#get()}获取的日期直接做减法，要考虑跨年跨月等情况
     * 
     * @param dateOld 较早的日期
     * @param dateNew 较晚的日期
     * @return 参数{@code dateOld}和参数{@code dateNew}相隔的天数
     */
    public static int getIntervalBetweenTwoDay(Date dateOld, Date dateNew) {
        Calendar calOld = Calendar.getInstance();
        calOld.setTime(dateOld);
        eraseTime(calOld);

        Calendar calNew = Calendar.getInstance();
        calNew.setTime(dateNew);
        eraseTime(calNew);

        long diffInMillis = calNew.getTimeInMillis() - calOld.getTimeInMillis();
        return (int) (diffInMillis / MILLIS_PER_DAY);
    }

    /**
     * 计算2个日期之间隔了多少天
     * <p>
     * 将2个日期均推至各自的0点0分0秒0毫秒，然后得出2个日期的毫秒差，经过换算得到的值即2者天数之差
     * <p>
     * 不能直接使用{@link Calendar#get()}获取的日期直接做减法，要考虑跨年跨月等情况
     * 
     * @param timeOld 较早的日期
     * @param timeNew 较晚的日期
     * @return 参数{@code dateOld}和参数{@code dateNew}相隔的天数
     */
    public static int getIntervalBetweenTwoDay(long timeOld, long timeNew) {
        Calendar calOld = Calendar.getInstance();
        calOld.setTimeInMillis(timeOld);
        eraseTime(calOld);

        Calendar calNew = Calendar.getInstance();
        calNew.setTimeInMillis(timeNew);
        eraseTime(calNew);

        long diffInMillis = calNew.getTimeInMillis() - calOld.getTimeInMillis();
        return (int) (diffInMillis / MILLIS_PER_DAY);
    }

    /**
     * 擦除时分秒等信息
     * 
     * @param cal
     */
    private static void eraseTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 计算现在时刻距离下一个指定日期(周、时、分、秒)最近的毫秒数
     * 
     * @param dayOfWeek 周，参考{@link Calendar#DAY_OF_WEEK}
     * @param hourOfDay 时,24小时制
     * @param minuteOfHour 分
     * @param secondOfMinute 秒
     * @return 距离下一个指定日期最近的毫秒数
     */
    public static long getWeekDelay(int dayOfWeek, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        now.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minuteOfHour);
        now.set(Calendar.SECOND, secondOfMinute);
        now.set(Calendar.MILLISECOND, 0);
        long thisMillis = now.getTimeInMillis();
        long diffMillis = thisMillis - nowMillis;
        if (diffMillis < 0) {
            return diffMillis + MILLIS_PER_WEEK;
        }
        return diffMillis;
    }

    /**
     * 计算现在时刻距离下一个指定时间(时、分、秒)最近的毫秒数
     * 
     * @param hourOfDay 时,24小时制
     * @param minuteOfHour 分
     * @param secondOfMinute 秒
     * @return 距离下一个指定时间最近的毫秒数
     */
    public static long getDayDelay(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minuteOfHour);
        now.set(Calendar.SECOND, secondOfMinute);
        now.set(Calendar.MILLISECOND, 0);
        long destMillis = now.getTimeInMillis();
        long diffMillis = destMillis - nowMillis;
        if (diffMillis < 0) {
            return diffMillis + MILLIS_PER_DAY;
        }
        return diffMillis;
    }

    /**
     * 计算现在时刻距离下一个指定时间(日、时、分、秒)最近的毫秒数
     * 
     * @param dayOfMonth 日(相对月)
     * @param hourOfDay 时，24小时制
     * @param minute 分
     * @param second 秒
     * @return 距离下一个指定时间(日、时、分、秒)最近的毫秒数
     */
    public static long getMonthDelay(int dayOfMonth, int hourOfDay, int minute, int second) {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, second);
        now.set(Calendar.MILLISECOND, 0);
        long thisMillis = now.getTimeInMillis();
        long diffMillis = thisMillis - nowMillis;
        if (diffMillis < 0) {
            now.add(Calendar.MONTH, 1);
            return now.getTimeInMillis() - nowMillis;
        }
        return diffMillis;
    }

    /**
     * 计算现在时刻距离下一个指定时间(月、日、时、分、秒)最近的毫秒数
     * 
     * @param monthOfyear 月(相对年)
     * @param dayOfMonth 日(相对月)
     * @param hourOfDay 时，24小时制
     * @param minute 分
     * @param second 秒
     * @return 距离下一个指定时间(日、时、分、秒)最近的毫秒数
     */
    public static long getYearDelay(int monthOfyear, int dayOfMonth, int hourOfDay, int minute, int second) {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        now.set(Calendar.MONTH, monthOfyear);
        now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, second);
        now.set(Calendar.MILLISECOND, 0);
        long thisMillis = now.getTimeInMillis();
        long diffMillis = thisMillis - nowMillis;
        if (diffMillis < 0) {
            now.add(Calendar.YEAR, 1);
            return now.getTimeInMillis() - nowMillis;
        }
        return diffMillis;
    }
}
