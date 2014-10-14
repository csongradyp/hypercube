package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class MappingRequest implements IEvent {

    private final String account;
    private final Path localFolder;
    private final Path remoteFolder;

    public MappingRequest(final String account, final Path localFolder, final Path remoteFolder) {
        this.account = account;
        this.localFolder = localFolder;
        this.remoteFolder = remoteFolder;
    }

    public Path getLocalFolder() {
        return localFolder;
    }

    public Path getRemoteFolder() {
        return remoteFolder;
    }

    public String getAccount() {
        return account;
    }
}
