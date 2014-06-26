package com.noe.hypercube.ui.desktop.observer;

import com.sun.istack.internal.NotNull;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

public class StorageCheckTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCheckTask.class);

    private List<Path> lastCheckedRoots;
    private final List<StorageEventTask> attachTasks;
    private final List<StorageEventTask> detachTasks;

    public StorageCheckTask(@NotNull List<StorageEventTask> attachTasks, @NotNull List<StorageEventTask> detachTasks) {
        this.attachTasks = attachTasks;
        this.detachTasks = detachTasks;
        lastCheckedRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
    }

    @Override
    public void run() {
        List<Path> roots = lastCheckedRoots;
        List<Path> newRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
        for (Path newRoot : newRoots) {
            if (!roots.contains(newRoot)) {
                LOG.info("Drive has been detected : {}", newRoot);
                runAttachTasks(newRoot);
            } else {
                roots.remove(newRoot);
            }
        }
        if (!roots.isEmpty()) {
            for (Path root : roots) {
                LOG.info("Drive has been removed : {}", root);
                runDetachTasks(root);
            }
        }
        lastCheckedRoots = newRoots;
    }

    private void runDetachTasks(Path detachedStorage) {
        for (StorageEventTask detachTask : detachTasks) {
            detachTask.run(detachedStorage);
        }
    }

    private void runAttachTasks(Path attachedStorage) {
        for (StorageEventTask attachTask : attachTasks) {
            attachTask.run(attachedStorage);
        }
    }
}
