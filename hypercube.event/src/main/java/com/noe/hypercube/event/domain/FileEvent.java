package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileEvent implements IEvent {

    private FileEventType eventType;
    private Path localPath;
    private Path remotePath;

    public FileEvent(final Path localPath, final Path remotePath, final FileEventType eventType) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.eventType = eventType;
    }

    public FileEventType getEventType() {
        return eventType;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public Path getRemotePath() {
        return remotePath;
    }
}
