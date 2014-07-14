package com.noe.hypercube.ui.domain;

public class LocalFile extends File {

    private final java.io.File file;

    public LocalFile( java.io.File file ) {
        super(file.toPath());
        this.file  = file;
    }

    public LocalFile( String path ) {
        this(new java.io.File( path ));
    }

    public LocalFile(final String path, final boolean stepBack) {
        this(path);
        setStepBack(stepBack);
    }

    public LocalFile( java.io.File parentFile, boolean stepBack ) {
        this( parentFile);
        setStepBack( stepBack );
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

    @Override public boolean isRoot() {
        return file.getParentFile().getParentFile() == null;
    }

}
