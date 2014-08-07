package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileListRequest implements IEvent {

    private final Path remoteFolder;

    public FileListRequest( Path remoteFolder ) {
        this.remoteFolder = remoteFolder;
    }

    public Path getRemoteFolder() {
        return remoteFolder;
    }
}
