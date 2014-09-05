package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileListRequest implements IEvent {

    private final String account;
    private Path folder;
    private Path previousFolder;

    public FileListRequest(String account, final Path remoteFolder, Path previousFolder) {
        this.account = account;
        this.folder = remoteFolder;
        this.previousFolder = previousFolder;
    }

    public FileListRequest(String account) {
        this.account = account;
    }

    public Path getFolder() {
        return folder;
    }

    public Path getPreviousFolder() {
        return previousFolder;
    }

    public String getAccount() {
        return account;
    }

    public boolean isCloud() {
        return account.equals("Cloud");
    }
}
