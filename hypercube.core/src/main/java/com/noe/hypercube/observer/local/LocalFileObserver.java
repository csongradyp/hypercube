package com.noe.hypercube.observer.local;

import com.noe.hypercube.synchronization.presynchronization.FilePreSynchronizer;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
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

    public LocalFileObserver(Path targetDir, LocalFileListener listener, FilePreSynchronizer preSynchronizer) {
        super(targetDir.toFile());
        this.targetDir = targetDir;
        this.listener = listener;
        addListener(listener);
        preSynchronizer.run(FileUtils.listFiles(targetDir.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
    }

    public Path getTargetDir() {
        return targetDir;
    }

    public LocalFileListener getListener() {
        return listener;
    }

    public IUploader getUploader() {
        return listener.getUploader();
    }
}
