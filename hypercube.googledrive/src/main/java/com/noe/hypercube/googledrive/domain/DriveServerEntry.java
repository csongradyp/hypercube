package com.noe.hypercube.googledrive.domain;

import com.google.api.services.drive.model.File;
import com.noe.hypercube.domain.ServerEntry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class DriveServerEntry implements ServerEntry {

    private File remoteFile;
    private Path path;
    private String revision;
    private Date lastModified;
    private boolean isFolder;

    public DriveServerEntry(final String path, final String revision, final Date lastModified, final boolean isFolder) {
        this.path = Paths.get(path);
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    public DriveServerEntry(final File remoteFile, final String path, final String revision, final Date lastModified) {
        this.remoteFile = remoteFile;
        this.revision = revision;
        this.lastModified = lastModified;
    }

    public File getRemoteFile() {
        return remoteFile;
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
