package core.fire.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SynchronizedEventSubscriber extends EventSubscriber
{
    /**
     * Creates a new SynchronizedEventSubscriber to wrap {@code method} on
     * {@code target}.
     *
     * @param target object to which the method applies.
     * @param method subscriber method.
     */
    public SynchronizedEventSubscriber(Object target, Method method) {
        super(target, method);
    }

    @Override
    public void handleEvent(Object event) throws InvocationTargetException {
        // https://code.google.com/p/guava-libraries/issues/detail?id=1403
        synchronized (this) {
            super.handleEvent(event);
        }
    }
}