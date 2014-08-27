package com.noe.hypercube.ui.domain;

import java.nio.file.Path;

public class StepBackFile extends File {

    public StepBackFile(final Path path) {
        super(path);
        setStepBack(true);
    }

    public StepBackFile(final java.io.File parentFile) {
        super(parentFile.toPath());
        setStepBack(true);
    }


    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Stepback file holds only the parent file information");
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public long size() {
        return 0L;
    }

    @Override
    public long lastModified() {
        return 0L;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

}
