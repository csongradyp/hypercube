package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;
import java.nio.file.Path;

public class DownloadRequest extends AccountActionEvent {

    private final Path remoteFile;
    private final Path localFolder;
    private Boolean deleteFileAfterDownload;

    public DownloadRequest(final String account, final Path remoteFile, final Path localFolder) {
        super(account);
        this.remoteFile = remoteFile;
        this.localFolder = localFolder;
        deleteFileAfterDownload = false;
    }

    public DownloadRequest(final String account, final Path remoteFile, final Path localFolder, final Boolean deleteFileAfterDownload) {
        this(account, remoteFile, localFolder);
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
