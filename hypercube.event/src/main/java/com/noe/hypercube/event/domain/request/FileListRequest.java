package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;

import java.nio.file.Path;

public class FileListRequest extends AccountActionEvent {

    private Path folder;
    private Path previousFolder;
    private final Integer target;

    public FileListRequest(final Integer target, final String account, final Path remoteFolder, final Path previousFolder) {
       super(account);
        this.target = target;
        this.folder = remoteFolder;
        this.previousFolder = previousFolder;
    }

    public FileListRequest(final Integer target, final String account) {
        super(account);
        this.target = target;
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

    public Integer getTarget() {
        return target;
    }
}
