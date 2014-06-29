package com.noe.hypercube.observer.local.storage;

import java.nio.file.Path;

public class StorageEvent {

    private Path storage;
    private StorageEventType event;

    public StorageEvent(Path storage, StorageEventType event) {
        this.storage = storage;
        this.event = event;
    }

    public Path getStorage() {
        return storage;
    }

    public StorageEventType getEvent() {
        return event;
    }

    public boolean isAttached() {
        return StorageEventType.ATTACHED.equals(event);
    }

    public boolean isDetached() {
        return StorageEventType.DETACHED.equals(event);
    }
}
