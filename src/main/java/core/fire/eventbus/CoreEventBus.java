/**
 * 
 */
package core.fire.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * 基于Guava的EventBus
 * 
 * @author lihuoliang
 *
 */
public class CoreEventBus
{
    private static EventBus eventbus = new EventBus();

    /**
     * 抛出事件
     * 
     * @param event
     */
    public static void post(Object event) {
        eventbus.post(event);
    }

    /**
     * 注册事件处理器
     * 
     * @param object
     */
    public static void register(Object object) {
        eventbus.register(object);
    }
}
