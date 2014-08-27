package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileListRequest implements IEvent {

    private final String account;
    private Path parentRemoteFolder;
    private Path remoteFolder;

    public FileListRequest(String account, final Path remoteFolder) {
        this.account = account;
        this.remoteFolder = remoteFolder;
    }

    public FileListRequest(String account, final Path parentRemoteFolder, final Path remoteFolder) {
        this.account = account;
        this.parentRemoteFolder = parentRemoteFolder;
        this.remoteFolder = remoteFolder;
    }

    public FileListRequest(String account) {
        this.account = account;
    }

    public Path getRemoteFolder() {
        return remoteFolder;
    }

    public Path getParentRemoteFolder() {
        return parentRemoteFolder;
    }

    public String getAccount() {
        return account;
    }
}
