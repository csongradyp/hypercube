package com.noe.hypercube.event.subscriber;

import com.noe.hypercube.event.EventBus;

import javax.annotation.PostConstruct;
import java.util.List;

public class FileEventSubscriber {

    private final List listeners;

    public FileEventSubscriber(List listeners) {
        this.listeners = listeners;
    }

    @PostConstruct
    public void subscribeAll() {
        for(Object listener : listeners) {
            EventBus.subscribeToFileEvent(listener);
        }
    }

}
