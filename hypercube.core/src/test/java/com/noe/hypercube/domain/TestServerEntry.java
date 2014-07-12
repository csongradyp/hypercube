package com.noe.hypercube.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class TestServerEntry implements ServerEntry {

    private Path path;
    private String revision;
    private Date lastModified;
    private boolean isFolder;

    public TestServerEntry(final String path, final String revision) {
        this(path, revision, new Date(), false);
    }

    public TestServerEntry(final String path, final String revision, final Date lastModified, final boolean isFolder) {
        this.path = Paths.get(path);
        this.revision = revision;
        this.lastModified = lastModified;
        this.isFolder = isFolder;
    }

    @Override
    public String getRevision() {
        return null;
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
