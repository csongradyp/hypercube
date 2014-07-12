package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.IEvent;
import net.engio.mbassy.listener.Handler;

public interface EventHandler<EVENT_TYPE extends IEvent> {

    @Handler
    void handle(EVENT_TYPE event);
}
