package core.fire;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自命名的线程工厂类，一般配合线程池使用
 * 
 * @see java.util.concurrent.Executors.DefaultThreadFactory
 * 
 * @author lhl
 *
 */
public class NamedThreadFactory implements ThreadFactory
{
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix; // 命名前缀

    public NamedThreadFactory(String threadNamePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = threadNamePrefix + "-pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    /**
     * 生成一个新线程
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
