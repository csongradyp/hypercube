package com.noe.hypercube.event.dto;

import java.nio.file.Path;
import java.util.Date;

public class RemoteFileInfo {

    private final Path localPath;
    private final Path remotePath;
    private final Long size;
    private final Date timeStamp;
    private final String accountName;
    private final Boolean directory;

    public RemoteFileInfo(final String accountName, final Path remotePath,final Long size, final Path localPath,  final Date timeStamp, final Boolean directory) {
        this.accountName = accountName;
        this.remotePath = remotePath;
        this.size = size;
        this.localPath = localPath;
        this.timeStamp = timeStamp;
        this.directory = directory;
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

    public Boolean isDirectory() {
        return directory;
    }

    public Long getSize() {
        return size;
    }
}
