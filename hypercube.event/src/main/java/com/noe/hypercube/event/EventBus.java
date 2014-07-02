package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StorageEvent;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

public final class EventBus {

    private final static MBassador<StorageEvent> storageEventBus = new MBassador<>(BusConfiguration.Default());;
    private final static MBassador<FileEvent> fileEventBus = new MBassador<>(BusConfiguration.Default());

    private EventBus() {
    }

    public static void publish(FileEvent fileEvent) {
        fileEventBus.publishAsync(fileEvent);
    }

    public static void publish(StorageEvent storageEvent) {
        storageEventBus.publishAsync(storageEvent);
    }

    public static void subscribeToFileEvent(Object listener) {
        fileEventBus.subscribe(listener);
    }

    public static void subscribeToStorageEvent(Object listener) {
        storageEventBus.subscribe(listener);
    }

    public static void unsubscribeToFileEvent(Object listener) {
        fileEventBus.unsubscribe(listener);
    }

    public static void unsubscribeToStorageEvent(Object listener) {
        storageEventBus.unsubscribe(listener);
    }
}
