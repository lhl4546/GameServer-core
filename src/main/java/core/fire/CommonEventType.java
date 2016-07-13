/**
 * 
 */
package core.fire;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import core.fire.util.TimeUtil;

/**
 * 常用活动类型(按循环时间划分，比如每天、每月、每周...)
 * 
 * <p>
 * 活动时间配置规则(1月-12月分别对应1-12，周一-周日分别对应1-7)
 * <p>
 * 开启时间 - 结束时间
 * <ol>
 * <li>永不开启</li>
 * <li>永久开启: 无需配置起止时间</li>
 * <li>每日循环: HH:mm:ss - HH:mm:ss</li>
 * <li>每周循环: u HH:mm:ss - u HH:mm:ss</li>
 * <li>每月循环: dd HH:mm:ss - dd HH:mm:ss</li>
 * <li>每年循环: MM-dd HH:mm:ss - MM-dd HH:mm:ss</li>
 * </ol>
 * 
 * @author lhl
 *
 *         2015年12月31日 上午9:16:09
 */
public enum CommonEventType {
    /**
     * 永不开启
     */
    NEVER {
        @Override
        public long calcDelay(String time) throws Exception {
            return 0;
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return null;
        }

        @Override
        public boolean isNowOpened(String beginTime, String endTime) throws Exception {
            return false;
        }
    },

    /**
     * 永久开启
     */
    FOREVER {
        @Override
        public long calcDelay(String time) throws Exception {
            return 0;
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return null;
        }

        @Override
        public boolean isNowOpened(String beginTime, String endTime) throws Exception {
            return true;
        }
    },

    /** 每日 */
    DAILY {
        @Override
        public long calcDelay(String time) throws Exception {
            SimpleDateFormat fmt = getFormatter();
            Date date = fmt.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return TimeUtil.getDayDelay(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    },

    /** 每周 */
    WEEKLY {
        @Override
        public long calcDelay(String time) throws Exception {
            SimpleDateFormat fmt = getFormatter();
            Date date = fmt.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return TimeUtil.getWeekDelay(cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return new SimpleDateFormat("u HH:mm:ss");
        }
    },

    /** 每月 */
    MONTHLY {
        @Override
        public long calcDelay(String time) throws Exception {
            SimpleDateFormat fmt = getFormatter();
            Date date = fmt.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return TimeUtil.getMonthDelay(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return new SimpleDateFormat("dd HH:mm:ss");
        }
    },

    /** 每年 */
    YEARLY {
        @Override
        public long calcDelay(String time) throws Exception {
            SimpleDateFormat fmt = getFormatter();
            Date date = fmt.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return TimeUtil.getYearDelay(cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        @Override
        public SimpleDateFormat getFormatter() {
            return new SimpleDateFormat("MM-dd HH:mm:ss");
        }
    };

    /**
     * 计算延时
     * 
     * @param time 时间格式由循环类型决定
     * @return
     * @throws Exception
     */
    public abstract long calcDelay(String time) throws Exception;

    /**
     * 返回时间格式化
     * 
     * @return
     */
    abstract SimpleDateFormat getFormatter();

    /**
     * 当前是否已经开启
     * <p>
     * 将当前时间格式化为与活动时间类型格式一致的字符串，然后按字典排序直接比较字符串。
     * <p>
     * 注意，时间格式从大到小排列，年 月 日\周 时 分 秒，这样比较字符串才有意义
     * 
     * @param beginTime 时间格式由循环类型决定
     * @param endTime 时间格式由循环类型决定
     * @return 正好处于开启时间段内则返回true
     * @throws Exception
     */
    public boolean isNowOpened(String beginTime, String endTime) throws Exception {
        SimpleDateFormat fmt = getFormatter();
        String now = fmt.format(new Date());
        return now.compareTo(beginTime) >= 0 && now.compareTo(endTime) < 0;
    }
}
