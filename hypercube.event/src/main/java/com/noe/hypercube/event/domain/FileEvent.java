package com.noe.hypercube.event.domain;

import java.nio.file.Path;
import java.util.Date;

public class FileEvent implements IEvent {

    private final FileEventType eventType;
    private final Path localPath;
    private final Path remotePath;
    private final Date timeStamp;
    private final String accountName;

    public FileEvent(final String accountName, final Path localPath, final Path remotePath, final FileEventType eventType) {
        this.accountName = accountName;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.eventType = eventType;
        timeStamp = new Date();
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

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getAccountName() {
        return accountName;
    }
}
