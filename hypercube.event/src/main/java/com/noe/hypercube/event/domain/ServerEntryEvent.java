package com.noe.hypercube.event.domain;


import com.noe.hypercube.domain.ServerEntry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ServerEntryEvent implements ServerEntry {

    private Path path;
    private String revision;
    private Date lastModified;
    private boolean isFolder;

    public ServerEntryEvent(String path, String revision, Date lastModified, boolean isFolder) {
        this.path = Paths.get(path);
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
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

    @Override
    public String toString() {
        return "Dropbox File [ "
                + path
                + ", rev: " + revision
                + ", lastModDate: " + lastModified
                + " ]";
    }
}
