package com.noe.hypercube.event.domain;

import java.nio.file.Path;

import static com.noe.hypercube.event.domain.StorageEventType.ATTACHED;
import static com.noe.hypercube.event.domain.StorageEventType.DETACHED;

public class StorageEvent implements IEvent {

    private Path storage;
    private StorageEventType event;

    public StorageEvent(Path storage, StorageEventType event) {
        this.storage = storage;
        this.event = event;
    }

    public Path getStorage() {
        return storage;
    }

    public boolean isAttached() {
        return ATTACHED.equals(event);
    }

    public boolean isDetached() {
        return DETACHED.equals(event);
    }
}
