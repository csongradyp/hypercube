package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileListRequest extends AccountActionEvent {

    private Path folder;
    private Path previousFolder;

    public FileListRequest(final String account, final Path remoteFolder, final Path previousFolder) {
       super(account);
        this.folder = remoteFolder;
        this.previousFolder = previousFolder;
    }

    public FileListRequest(final String account) {
        super(account);
    }

    public Path getFolder() {
        return folder;
    }

    public Path getPreviousFolder() {
        return previousFolder;
    }

    public boolean isCloud() {
        return getAccount().equals("Cloud");
    }
}
