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
    private String id;

    public BoxServerEntry(String path, String revision, Date lastModified, boolean isFolder) {
        this.path = Paths.get(path);
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    public BoxServerEntry(String path, boolean isFolder) {
        this.path = Paths.get(path);
        this.isFolder = isFolder;
    }

    public BoxServerEntry(String path, Long size, String revision, Date lastModified, boolean isFolder) {
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

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Date lastModified() {
        return lastModified;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getAccount() {
        return Box.name;
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
