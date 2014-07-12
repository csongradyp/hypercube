package com.noe.hypercube.event.subscriber;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;

import javax.annotation.PostConstruct;
import java.util.List;

public class FileEventSubscriber {

    private final List<EventHandler<FileEvent>> listeners;

    public FileEventSubscriber(final List<EventHandler<FileEvent>> listeners) {
        this.listeners = listeners;
    }

    @PostConstruct
    public void subscribeAll() {
        for(EventHandler<FileEvent> listener : listeners) {
            EventBus.subscribeToFileEvent(listener);
        }
    }
}
