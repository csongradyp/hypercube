package com.noe.hypercube.event.subscriber;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.StorageEvent;

import javax.annotation.PostConstruct;
import java.util.List;

public class StorageEventBeanSubscriber {

    private final List<EventHandler<StorageEvent>> listeners;

    public StorageEventBeanSubscriber(List<EventHandler<StorageEvent>> listeners) {
        this.listeners = listeners;
    }

    @PostConstruct
    public void subscribeAll() {
        for(EventHandler<StorageEvent> listener : listeners) {
            EventBus.subscribeToStorageEvent(listener);
        }
    }
}
