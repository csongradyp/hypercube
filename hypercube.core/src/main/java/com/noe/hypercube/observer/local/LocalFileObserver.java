package com.noe.hypercube.observer.local;

import com.noe.hypercube.synchronization.presynchronization.IFilePreSynchronizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

public class LocalFileObserver extends FileAlterationObserver {

    private final Path targetDir;
    private final FileAlterationListener listener;

    public LocalFileObserver(final Path targetDir, final FileAlterationListener listener, final IFilePreSynchronizer preSynchronizer) {
        super(targetDir.toFile());
        this.targetDir = targetDir;
        this.listener = listener;
        addListener(listener);
        preSynchronizer.run(FileUtils.listFiles(targetDir.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
    }

    public LocalFileObserver(final Path targetDir, final FileAlterationListener listener) {
        super(targetDir.toFile());
        this.targetDir = targetDir;
        this.listener = listener;
        addListener(listener);
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
