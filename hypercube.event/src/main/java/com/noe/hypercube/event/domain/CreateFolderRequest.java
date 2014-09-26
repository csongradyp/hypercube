package com.noe.hypercube.event.domain;


import java.nio.file.Path;

public class CreateFolderRequest extends AccountActionEvent {

    private final Path baseFolder;
    private final String folderName;

    public CreateFolderRequest(final String account, final Path baseFolder, final String folderName) {
        super(account);
        this.baseFolder = baseFolder;
        this.folderName = folderName;
    }

    public Path getBaseFolder() {
        return baseFolder;
    }

    public String getFolderName() {
        return folderName;
    }
}
