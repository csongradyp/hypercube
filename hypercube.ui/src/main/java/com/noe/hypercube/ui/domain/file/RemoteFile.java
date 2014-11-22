package com.noe.hypercube.ui.domain.file;

import java.nio.file.Path;
import java.util.Date;

public class RemoteFile extends File {

    private final boolean directory;
    private final long size;
    private final Date lastModified;
    private final String origin;
    private String id;

    public RemoteFile(final String origin, final Path path, final long size, final boolean directory, final Date lastModified) {
        super(path);
        this.origin = origin;
        this.directory = directory;
        this.size = size;
        this.lastModified = lastModified;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public long lastModified() {
        if(lastModified == null) {
            return 0L;
        }
        return lastModified.getTime();
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isShared() {
        return sharedWith().size() > 1;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
