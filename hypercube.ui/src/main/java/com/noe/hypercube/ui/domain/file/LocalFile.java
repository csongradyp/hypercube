package com.noe.hypercube.ui.domain.file;

import java.nio.file.Path;

public class LocalFile extends File {

    private final java.io.File file;

    public LocalFile(Path path) {
        super(path);
        this.file = path.toFile();
    }

    public java.io.File getFile() {
        return file;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public long size() {
        return file.length();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public boolean isRoot() {
        return file.getParentFile().getParentFile() == null;
    }

    @Override
    public String getOrigin() {
        return "Local";
    }

}
