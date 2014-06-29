package com.noe.hypercube.observer.local.storage;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static com.noe.hypercube.observer.local.storage.StorageEventType.*;

public class StorageCheckTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCheckTask.class);

    private List<Path> lastCheckedRoots;
    private MBassador<StorageEvent> bus;

    public StorageCheckTask() {
        lastCheckedRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
        bus = new MBassador<>(BusConfiguration.Default());
    }

    @Override
    public void run() {
        List<Path> roots = lastCheckedRoots;
        List<Path> newRoots = IteratorUtils.toList(FileSystems.getDefault().getRootDirectories().iterator());
        for (Path newRoot : newRoots) {
            if (!roots.contains(newRoot)) {
                LOG.info("Drive has been detected : {}", newRoot);
                bus.publishAsync(new StorageEvent(newRoot, ATTACHED));
            } else {
                roots.remove(newRoot);
            }
        }
        if (!roots.isEmpty()) {
            for (Path root : roots) {
                LOG.info("Drive has been removed : {}", root);
                bus.publishAsync(new StorageEvent(root, DETACHED));
            }
        }
        lastCheckedRoots = newRoots;
    }

}
