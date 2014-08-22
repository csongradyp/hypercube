package com.noe.hypercube.ui.domain;

import java.nio.file.Path;

public class LocalFile extends File {

    private final java.io.File file;

    public LocalFile(java.io.File file) {
        super(file.toPath());
        this.file = file;
    }

    public LocalFile(Path path) {
        super(path);
        this.file = path.toFile();
    }

    public LocalFile(String path) {
        this(new java.io.File(path));
    }

    public LocalFile(final String path, final boolean stepBack) {
        this(path);
        setStepBack(stepBack);
    }

    public LocalFile(java.io.File parentFile, boolean stepBack) {
        this(parentFile);
        setStepBack(stepBack);
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


}
