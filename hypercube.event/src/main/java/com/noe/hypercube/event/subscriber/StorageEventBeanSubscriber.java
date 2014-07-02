package com.noe.hypercube.event.subscriber;

import com.noe.hypercube.event.EventBus;

import javax.annotation.PostConstruct;
import java.util.List;

public class StorageEventBeanSubscriber {

    private final List listeners;

    public StorageEventBeanSubscriber(List listeners) {
        this.listeners = listeners;
    }

    @PostConstruct
    public void subscribeAll() {
        for(Object listener : listeners) {
            EventBus.subscribeToStorageEvent(listener);
        }
    }
}
