package com.noe.hypercube.googledrive.domain;

import com.google.api.services.drive.model.File;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.googledrive.service.DriveDirectoryUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class DriveServerEntry implements ServerEntry {

    private final String id;
    private Path path;
    private final String revision;
    private final Date lastModified;
    private final boolean isFolder;

    public DriveServerEntry(final String path, String id, final String revision, final Date lastModified, final boolean isFolder) {
        this.path = Paths.get(path);
        this.id = id;
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean isFolder() {
        return isFolder;
    }

    @Override
    public boolean isFile() {
        return !isFolder;
    }

    @Override
    public Date lastModified() {
        return lastModified;
    }
}
