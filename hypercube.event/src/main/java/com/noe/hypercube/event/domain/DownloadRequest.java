package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class DownloadRequest implements IEvent {

    private final Path remoteFile;
    private final Path localFolder;
    private Boolean deleteFileAfterDownload;

    public DownloadRequest(final Path remoteFile, final Path localFolder) {
        this.remoteFile = remoteFile;
        this.localFolder = localFolder;
        deleteFileAfterDownload = false;
    }

    public DownloadRequest(final Path remoteFile, final Path localFolder, final Boolean deleteFileAfterDownload) {
        this(remoteFile, localFolder);
        this.deleteFileAfterDownload = deleteFileAfterDownload;
    }

    public Path getRemoteFile() {
        return remoteFile;
    }

    public Path getLocalFolder() {
        return localFolder;
    }

    public Boolean deleteFileAfterDownload() {
        return deleteFileAfterDownload;
    }
}
