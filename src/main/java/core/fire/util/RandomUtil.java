/**
 * 
 */
package core.fire.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lhl
 *
 *         2015年8月12日上午9:58:16
 */
final public class RandomUtil
{
    private RandomUtil() {
    }

    /**
     * [0, 1)
     * 
     * @return
     */
    public static float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    /**
     * [origin, bound)
     * 
     * @param origin
     * @param bound
     * @return
     */
    public static int nextInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }
}
