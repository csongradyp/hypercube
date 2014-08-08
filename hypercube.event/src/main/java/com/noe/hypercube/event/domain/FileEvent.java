package com.noe.hypercube.event.domain;

import com.noe.hypercube.event.domain.type.FileActionType;
import com.noe.hypercube.event.domain.type.FileEventType;
import com.noe.hypercube.event.domain.type.StreamDirection;

import java.nio.file.Path;
import java.util.Date;

public class FileEvent implements IEvent {

    private final Path localPath;
    private final Path remotePath;
    private final Date timeStamp;
    private final String accountName;
    private FileActionType actionType;
    private StreamDirection direction;
    private FileEventType eventType;

    public FileEvent(final String accountName, final Path localPath, final Path remotePath, final FileActionType actionType) {
        this.accountName = accountName;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.actionType = actionType;
        timeStamp = new Date();
        eventType = FileEventType.SUBMITTED;
    }

    public void setDirection(final StreamDirection direction) {
        this.direction = direction;
    }

    public void setStarted() {
        eventType = FileEventType.STARTED;
    }

    public void setFinished() {
        eventType = FileEventType.FINISHED;
    }

    public Boolean isStarted() {
        return eventType == FileEventType.STARTED;
    }

    public Boolean isSubmitted() {
        return eventType == FileEventType.SUBMITTED;
    }

    public Boolean isFinished() {
        return eventType == FileEventType.FINISHED;
    }

    public FileActionType getActionType() {
        return actionType;
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

    public StreamDirection getDirection() {
        return direction;
    }

    public FileEventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return localPath.getFileName().toString();
    }
}
