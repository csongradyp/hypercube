package com.noe.hypercube.observer.local.storage;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.StorageEvent;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static com.noe.hypercube.event.domain.StorageEventType.ATTACHED;
import static com.noe.hypercube.event.domain.StorageEventType.DETACHED;

public class StorageCheckTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCheckTask.class);

    private List<Path> lastCheckedRoots;

    public StorageCheckTask() {
        lastCheckedRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
    }

    @Override
    public void run() {
        List<Path> roots = lastCheckedRoots;
        List<Path> newRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
        for (Path newRoot : newRoots) {
            if (!roots.contains(newRoot)) {
                LOG.info("Drive has been detected : {}", newRoot);
                EventBus.publish(new StorageEvent(newRoot, ATTACHED));
            } else {
                roots.remove(newRoot);
            }
        }
        if (!roots.isEmpty()) {
            for (Path root : roots) {
                LOG.info("Drive has been removed : {}", root);
                EventBus.publish(new StorageEvent(root, DETACHED));
            }
        }
        lastCheckedRoots = newRoots;
    }

}
