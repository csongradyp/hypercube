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
    private String id;
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
        this.id = remoteFile.getId();
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
    public Long getSize() {
        return null;
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
    public boolean isShared() {
        return false;
    }

    @Override
    public Date lastModified() {
        return lastModified;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getAccount() {
        return "Google Drive";
    }
}
