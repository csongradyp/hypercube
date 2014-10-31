package com.noe.hypercube.observer.local;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

public class LocalFileObserver extends FileAlterationObserver {

    private final Path targetDir;

    public LocalFileObserver(final Path targetDir, final FileAlterationListener listener) {
        super(targetDir.toFile());
        this.targetDir = targetDir;
        addListener(listener);
    }

    public Path getTargetDir() {
        return targetDir;
    }
}