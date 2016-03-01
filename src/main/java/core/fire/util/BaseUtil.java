/**
 * 
 */
package core.fire.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 *
 */
final public class BaseUtil
{
    private BaseUtil() {
    }

    /**
     * 关闭线程池，等待指定的时间，超时后将直接关闭
     * 
     * @param es 线程池
     * @param waitTimeInMs 关闭等待时间(毫秒数)
     */
    public static void shutdownThreadPool(ExecutorService es, int waitTimeInMs) {
        es.shutdown();
        try {
            if (!es.awaitTermination(waitTimeInMs, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
                es.awaitTermination(waitTimeInMs, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 集合是否为空(null或者empty)
     * 
     * @param t
     * @return true：空
     */
    public static boolean isNullOrEmpty(Collection<?> t) {
        return t == null || t.isEmpty();
    }

    /**
     * 拷贝一份全新的list
     * 
     * @param source
     * @return
     */
    public static <T> List<T> copyList(List<T> source) {
        if (source == null)
            return null;

        return new ArrayList<>(source);
    }

    /**
     * 字符串是否为空(null或者empty)
     * 
     * @param source
     * @return true：空
     */
    public static boolean isNullOrEmpty(String source) {
        return source == null || source.isEmpty();
    }

    /**
     * 分割字符串
     * 
     * @param source
     * @param regex
     * @return
     */
    public static String[] split(String source, String regex) {
        return source.split(regex);
    }

    /**
     * 按页获取列表元素
     * 
     * @param source 初始列表
     * @param pageSize 单页元素个数
     * @param page 页码(第几页)
     * @return 返回一个不可修改的列表，不会为null
     */
    public static <T> List<T> getByPage(List<T> source, int pageSize, int page) {
        if (isNullOrEmpty(source))
            return Collections.emptyList();

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(page * pageSize, source.size());

        if (startIndex < 0 || startIndex >= source.size())
            return Collections.emptyList();

        return Collections.unmodifiableList(source.subList(startIndex, endIndex));
    }

    /**
     * 计算总页数
     * 
     * @param size 总元素个数
     * @param pageSize 单页元素个数
     * @return 总页数
     */
    public static int getTotalPage(int size, int pageSize) {
        if (size <= 0)
            return 0;

        int totalPage = size / pageSize;
        if (size > totalPage * pageSize) {
            totalPage += 1;
        }

        return totalPage;
    }
}
