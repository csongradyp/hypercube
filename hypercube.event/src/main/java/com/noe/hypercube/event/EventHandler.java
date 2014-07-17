package com.noe.hypercube.event;

public interface EventHandler<EVENT_TYPE> {

    void onEvent(EVENT_TYPE event);
}
