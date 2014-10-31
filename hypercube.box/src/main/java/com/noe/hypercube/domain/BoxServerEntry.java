package com.noe.hypercube.domain;


import com.noe.hypercube.service.Box;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class BoxServerEntry implements ServerEntry {

    private Path path;
    private String revision;
    private Date lastModified;
    private Long size;
    private boolean isFolder;
    private final String id;

    public BoxServerEntry(String path, String id, String revision, Date lastModified, boolean isFolder) {
        this.id = id;
        this.path = Paths.get(path);
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    public BoxServerEntry(String path, String id, boolean isFolder) {
        this.id = id;
        this.path = Paths.get(path);
        this.isFolder = isFolder;
    }

    public BoxServerEntry(String path, String id, Long size, String revision, Date lastModified, boolean isFolder) {
        this.id = id;
        this.path = Paths.get(path);
        this.size = size;
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public Path getPath() {
        return path;
    }

    public void setSize(Long size) {
        this.size = size;
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

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
        return Box.getName();
    }

    @Override
    public String toString() {
        return "Box File [ "
                + path
                + ", rev: " + revision
                + ", lastModDate: " + lastModified
                + " ]";
    }
}
