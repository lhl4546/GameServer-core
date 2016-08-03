package core.fire.eventbus;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

public class SubscriberExceptionContext
{
    private final EventBus eventBus;
    private final Object event;
    private final Object subscriber;
    private final Method subscriberMethod;

    /**
     * @param eventBus The {@link EventBus} that handled the event and the
     *            subscriber. Useful for broadcasting a a new event based on the
     *            error.
     * @param event The event object that caused the subscriber to throw.
     * @param subscriber The source subscriber context.
     * @param subscriberMethod the subscribed method.
     */
    SubscriberExceptionContext(EventBus eventBus, Object event, Object subscriber, Method subscriberMethod) {
        this.eventBus = checkNotNull(eventBus);
        this.event = checkNotNull(event);
        this.subscriber = checkNotNull(subscriber);
        this.subscriberMethod = checkNotNull(subscriberMethod);
    }

    /**
     * @return The {@link EventBus} that handled the event and the subscriber.
     *         Useful for broadcasting a a new event based on the error.
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * @return The event object that caused the subscriber to throw.
     */
    public Object getEvent() {
        return event;
    }

    /**
     * @return The object context that the subscriber was called on.
     */
    public Object getSubscriber() {
        return subscriber;
    }

    /**
     * @return The subscribed method that threw the exception.
     */
    public Method getSubscriberMethod() {
        return subscriberMethod;
    }
}
