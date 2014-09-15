package com.noe.hypercube.domain;


import com.noe.hypercube.synchronization.Action;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadEntity {

    private final File file;
    private final Path remoteFolder;
    private final Action action;
    private final String origin;
    private boolean conflicted;

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
            return Paths.get(remoteFolder.toString(), createResolvedFileName());
        }
        return Paths.get(remoteFolder.toString(), file.getName());
    }

    private String createResolvedFileName() {
        final String ext = FilenameUtils.getExtension(file.toString());
        final String baseName = FilenameUtils.getBaseName(file.toString());
        return String.format("%s (%s).%s", baseName, origin, ext);
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
}
