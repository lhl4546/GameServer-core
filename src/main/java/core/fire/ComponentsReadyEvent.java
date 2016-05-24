package core.fire;

import org.springframework.context.ApplicationEvent;

/**
 * Event raised when all component gets started.
 * 
 * @author lhl
 *
 *         2016年5月24日 下午1:53:29
 */
public class ComponentsReadyEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 2381292617463631363L;

    public ComponentsReadyEvent(Object source) {
        super(source);
    }
}
