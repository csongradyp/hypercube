package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;

import java.nio.file.Path;

public class FileListRequest extends AccountActionEvent implements IFileListRequest {

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

    @Override
    public Path getFolder() {
        return folder;
    }

    public Path getPreviousFolder() {
        return previousFolder;
    }

    @Override
    public Boolean isCloud() {
        return getAccount().equals("Cloud");
    }

    @Override
    public Integer getTarget() {
        return target;
    }
}
