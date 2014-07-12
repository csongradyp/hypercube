package com.noe.hypercube.observer.local.storage;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.StorageEvent;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.noe.hypercube.event.domain.StorageEventType.ATTACHED;
import static com.noe.hypercube.event.domain.StorageEventType.DETACHED;

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
                newRoots.add(newRoot);
                EventBus.publish(new StorageEvent(newRoot, ATTACHED));
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

    private Iterable<Path> getCurrentStorages() {
        return FileSystems.getDefault().getRootDirectories();
    }

}
