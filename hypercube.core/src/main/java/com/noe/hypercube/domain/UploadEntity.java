package com.noe.hypercube.domain;


import com.noe.hypercube.synchronization.Action;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil.createResolvedFileName;

public class UploadEntity {

    private final File file;
    private final Path remoteFolder;
    private final Action action;
    private final String origin;
    private boolean conflicted;
    private boolean dependent;

    public UploadEntity(final File file, final Path remoteFolder, final Action action) {
        this.file = file;
        this.remoteFolder = remoteFolder;
        this.action = action;
        origin = "local";
        conflicted = false;
    }

    public UploadEntity(final File file, final Path remoteFolder, final String origin) {
        this.file = file;
        this.remoteFolder = remoteFolder;
        this.origin = origin;
        this.action = Action.ADDED;
        conflicted = false;
    }

    public void setConflicted(boolean conflicted) {
        this.conflicted = conflicted;
    }

    public Path getRemoteFilePath() {
        if (conflicted) {
            return Paths.get(remoteFolder.toString(), createResolvedFileName(this));
        }
        return Paths.get(remoteFolder.toString(), file.getName());
    }

    public File getFile() {
        return file;
    }

    public Path getRemoteFolder() {
        return remoteFolder;
    }

    public Action getAction() {
        return action;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean isDependent() {
        return dependent;
    }

    public void setDependent(Boolean dependent) {
        this.dependent = dependent;
    }
}
