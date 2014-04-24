package com.noe.hypercube.observer;

import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

public class LocalFileObserver extends FileAlterationObserver {

    private Path targetDir;
    private LocalFileListener listener;

    public LocalFileObserver(Path targetDir, LocalFileListener listener) {
        super(targetDir.toFile());
        this.targetDir = targetDir;
        this.listener = listener;
        addListener(listener);
    }

    public Path getTargetDir() {
        return targetDir;
    }

    public LocalFileListener getListener() {
        return listener;
    }
}
