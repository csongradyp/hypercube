package com.noe.hypercube.event.domain;


import java.nio.file.Path;

public class CreateFolderRequest implements IEvent {

    private final Path BaseFolder;
    private final String folderName;

    public CreateFolderRequest(Path baseFolder, String folderName) {
        BaseFolder = baseFolder;
        this.folderName = folderName;
    }

    public Path getBaseFolder() {
        return BaseFolder;
    }

    public String getFolderName() {
        return folderName;
    }
}
