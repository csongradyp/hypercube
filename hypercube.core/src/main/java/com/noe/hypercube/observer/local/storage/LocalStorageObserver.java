package com.noe.hypercube.observer.local.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LocalStorageObserver {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCheckTask.class);
    private static final long DELAY = 5L;
    private final ScheduledExecutorService service;

    public LocalStorageObserver() {
        service = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        LOG.info("Starting local storage observation");
        Runnable storageCheck = new StorageCheckTask();
        service.scheduleWithFixedDelay(storageCheck, 0L, DELAY, SECONDS);
        LOG.info("Local storage observation started - checking every {} seconds", DELAY);
    }

    public void stop() {
        service.shutdownNow();
    }

}
