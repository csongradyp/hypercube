package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.AccountEvent;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StateChangeEvent;
import com.noe.hypercube.event.domain.StorageEvent;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

public final class EventBus {

    private static final EventBus instance = new EventBus();

    private final MBassador<AccountEvent> accountEventBus;
    private final MBassador<StorageEvent> storageEventBus;
    private final MBassador<FileEvent> fileEventBus;
    private final MBassador<StateChangeEvent> stateEventBus;

    private EventBus() {
        accountEventBus = new MBassador<>(BusConfiguration.Default());
        storageEventBus = new MBassador<>(BusConfiguration.Default());
        fileEventBus = new MBassador<>(BusConfiguration.Default());
        stateEventBus = new MBassador<>(BusConfiguration.Default());
        registerShutdownHook(storageEventBus, fileEventBus, stateEventBus, accountEventBus);
    }

    private void registerShutdownHook(MBassador<?>... mBassadors) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (MBassador<?> eventBus : mBassadors) {
                eventBus.shutdown();
            }
        }));
    }

    public static void publish(FileEvent fileEvent) {
        instance.fileEventBus.publishAsync(fileEvent);
    }

    public static void publish(StorageEvent storageEvent) {
        instance.storageEventBus.publish(storageEvent);
    }

    public static void publish(StateChangeEvent stateChangeEvent) {
        instance.stateEventBus.publishAsync(stateChangeEvent);
    }

    public static void publish(AccountEvent accountEvent) {
        instance.accountEventBus.publish(accountEvent);
    }

    public static void subscribeToAccountEvent(EventHandler<AccountEvent> handler) {
        instance.accountEventBus.subscribe(handler);
    }

    public static void subscribeToStateEvent(EventHandler<StateChangeEvent> handler) {
        instance.stateEventBus.subscribe(handler);
    }

    public static void subscribeToFileEvent(EventHandler<FileEvent> handler) {
        instance.fileEventBus.subscribe(handler);
    }

    public static void unsubscribeToFileEvent(EventHandler<FileEvent> handler) {
        instance.fileEventBus.unsubscribe(handler);
    }

    public static void subscribeToStorageEvent(EventHandler<StorageEvent> handler) {
        instance.storageEventBus.subscribe(handler);
    }

    public static void unsubscribeToStorageEvent(EventHandler<StorageEvent> handler) {
        instance.storageEventBus.unsubscribe(handler);
    }
}
