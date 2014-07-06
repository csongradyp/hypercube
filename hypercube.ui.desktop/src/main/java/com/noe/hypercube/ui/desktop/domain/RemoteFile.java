package com.noe.hypercube.ui.desktop.domain;

import java.nio.file.Path;
import java.util.Date;

public class RemoteFile extends File {

    private final boolean directory;
    private final long size;
    private final Date lastModified;

    protected RemoteFile( final Path path, final long size, final boolean directory, final Date lastModified ) {
        super( path );
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
        return lastModified.getTime();
    }

    @Override public boolean isRoot() {
        return false;
    }
}
