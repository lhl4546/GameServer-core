package core.fire.eventbus;

public interface SubscriberExceptionHandler {
    /**
     * Handles exceptions thrown by subscribers.
     */
    void handleException(Throwable exception, SubscriberExceptionContext context);
  }