package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StorageEvent;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

public final class EventBus {

    private final static MBassador<StorageEvent> storageEventBus = new MBassador<>(BusConfiguration.Default());
    private final static MBassador<FileEvent> fileEventBus = new MBassador<>(BusConfiguration.Default());

    private EventBus() {
    }

    public static void publish(FileEvent fileEvent) {
        fileEventBus.publishAsync(fileEvent);
    }

    public static void publish(StorageEvent storageEvent) {
        storageEventBus.publishAsync(storageEvent);
    }

    public static void subscribeToFileEvent(EventHandler<FileEvent> handler) {
        fileEventBus.subscribe(handler);
    }

    public static void subscribeToStorageEvent(EventHandler<StorageEvent> handler) {
        storageEventBus.subscribe(handler);
    }

    public static void unsubscribeToFileEvent(EventHandler<FileEvent> handler) {
        fileEventBus.unsubscribe(handler);
    }

    public static void unsubscribeToStorageEvent(EventHandler<StorageEvent>  handler) {
        storageEventBus.unsubscribe(handler);
    }
}
