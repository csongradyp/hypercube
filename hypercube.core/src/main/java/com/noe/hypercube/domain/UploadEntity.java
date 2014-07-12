package com.noe.hypercube.domain;


import com.noe.hypercube.synchronization.Action;

import java.io.File;
import java.nio.file.Path;

public class UploadEntity {

    private final File file;
    private final Path remotePath;
    private final Action action;

    public UploadEntity(File file, Path remotePath, Action action) {
        this.file = file;
        this.remotePath = remotePath;
        this.action = action;
    }

    public File getFile() {
        return file;
    }

    public Path getRemotePath() {
        return remotePath;
    }

    public Action getAction() {
        return action;
    }
}
