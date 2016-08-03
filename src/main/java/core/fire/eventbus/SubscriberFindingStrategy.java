package core.fire.eventbus;

import com.google.common.collect.Multimap;

public interface SubscriberFindingStrategy
{
    /**
     * Finds all suitable event subscriber methods in {@code source}, organizes them
     * by the type of event they handle, and wraps them in {@link EventSubscriber} instances.
     *
     * @param source  object whose subscribers are desired.
     * @return EventSubscriber objects for each subscriber method, organized by event
     *         type.
     *
     * @throws IllegalArgumentException if {@code source} is not appropriate for
     *         this strategy (in ways that this interface does not define).
     */
    Multimap<Class<?>, EventSubscriber> findAllSubscribers(Object source);
}
