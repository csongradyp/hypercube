package com.noe.hypercube.event.domain.request;


import com.noe.hypercube.event.domain.AccountActionEvent;

import java.nio.file.Path;

public class CreateFolderRequest extends AccountActionEvent {

    private final Integer target;
    private final Path baseFolder;
    private final String folderName;

    public CreateFolderRequest(Integer target, final String account, final Path baseFolder, final String folderName) {
        super(account);
        this.target = target;
        this.baseFolder = baseFolder;
        this.folderName = folderName;
    }

    public Path getBaseFolder() {
        return baseFolder;
    }

    public String getFolderName() {
        return folderName;
    }

    public Integer getTarget() {
        return target;
    }
}
