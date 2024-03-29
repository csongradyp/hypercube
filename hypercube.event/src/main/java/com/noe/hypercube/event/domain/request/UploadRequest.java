package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;
import java.nio.file.Path;

public class UploadRequest extends AccountActionEvent {

    private final Path localFile;
    private final Path remoteFolder;
    private Boolean deleteFileAfterUpload;

    public UploadRequest(final String account, final Path localFile, final Path remoteFolder) {
        super(account);
        this.localFile = localFile;
        this.remoteFolder = remoteFolder;
        deleteFileAfterUpload = false;
    }

    public UploadRequest(final String account, final Path localFile, final Path remoteFolder, final Boolean deleteFileAfterUpload) {
        this(account, localFile, remoteFolder);
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
