package com.noe.hypercube.observer.local.storage;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.StorageEvent;
import com.noe.hypercube.event.domain.type.StorageEventType;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class StorageCheckTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCheckTask.class);

    private List<Path> lastCheckedRoots;

    public StorageCheckTask() {
        Iterable<Path> rootDirectories = getCurrentStorages();
        lastCheckedRoots = IteratorUtils.toList(rootDirectories.iterator());
    }

    @Override
    public void run() {
        final List<Path> roots = lastCheckedRoots;
        final Iterable<Path> rootDirectories = getCurrentStorages();
        final List<Path> newRoots = new ArrayList<>();

        for (Path newRoot : rootDirectories) {
            if (roots.contains(newRoot)) {
                newRoots.add(newRoot);
                roots.remove(newRoot);
            } else {
                LOG.info("Drive has been detected : {}", newRoot);
                final StorageEvent storageEvent = new StorageEvent(newRoot, StorageEventType.ATTACHED);
                EventBus.publish(storageEvent);
                newRoots.add(newRoot);
            }
        }
        if (!roots.isEmpty()) {
            for (Path root : roots) {
                LOG.info("Drive has been removed : {}", root);
                final StorageEvent storageEvent = new StorageEvent(root, StorageEventType.DETACHED);
                EventBus.publish(storageEvent);
            }
        }
        lastCheckedRoots = newRoots;
    }

    private Iterable<Path> getCurrentStorages() {
        return FileSystems.getDefault().getRootDirectories();
    }

}
