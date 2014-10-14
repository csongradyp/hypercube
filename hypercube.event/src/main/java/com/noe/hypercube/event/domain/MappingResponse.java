package com.noe.hypercube.event.domain;

public class MappingResponse implements IEvent {

    private final String account;
    private final String localFolder;
    private final String remoteFolder;

    public MappingResponse(final String account, final String localFolder, final String remoteFolder) {
        this.account = account;
        this.localFolder = localFolder;
        this.remoteFolder = remoteFolder;
    }

    public String getAccount() {
        return account;
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }
}
