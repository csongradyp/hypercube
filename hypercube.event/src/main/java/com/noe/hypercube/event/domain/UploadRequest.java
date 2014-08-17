package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class UploadRequest implements IEvent {

    private final Path localFile;
    private final Path remoteFolder;
    private Boolean deleteFileAfterUpload;

    public UploadRequest(final Path localFile, final Path remoteFolder) {
        this.localFile = localFile;
        this.remoteFolder = remoteFolder;
        deleteFileAfterUpload = false;
    }

    public UploadRequest(final Path localFile, final Path remoteFolder, final Boolean deleteFileAfterUpload) {
        this(localFile, remoteFolder);
        this.deleteFileAfterUpload = deleteFileAfterUpload;
    }

    public Path getLocalFile() {
        return localFile;
    }

    public Path getRemoteFolder() {
        return remoteFolder;
    }

    public Boolean deleteFileAfterUpload() {
        return deleteFileAfterUpload;
    }
}
