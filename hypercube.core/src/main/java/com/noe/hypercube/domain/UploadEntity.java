package com.noe.hypercube.domain;


import com.noe.hypercube.Action;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil.createResolvedFileName;

public class UploadEntity implements IStreamEntry<File> {

    private final File file;
    private final Path remoteFolder;
    private Action action;
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

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean isDependent() {
        return dependent;
    }

    @Override
    public File getDependency() {
        return file;
    }

    public void setDependent(Boolean dependent) {
        this.dependent = dependent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadEntity that = (UploadEntity) o;

        if (!file.equals(that.file)) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;
        return remoteFolder.equals(that.remoteFolder);

    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + remoteFolder.hashCode();
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("UploadEntity{file=%s, remoteFolder=%s, action=%s, origin='%s}", file, remoteFolder, action, origin);
    }
}
