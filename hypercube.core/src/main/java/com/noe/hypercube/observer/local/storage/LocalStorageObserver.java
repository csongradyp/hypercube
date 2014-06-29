package com.noe.hypercube.observer.local.storage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LocalStorageObserver {

    private final ScheduledExecutorService service;
    private final List<StorageCheckTask> attachTasks;
    private final List<StorageCheckTask> detachTasks;

    public LocalStorageObserver() {
        service = Executors.newSingleThreadScheduledExecutor();
        attachTasks = new CopyOnWriteArrayList<>();
        detachTasks = new CopyOnWriteArrayList<>();
    }

    public void onStorageAttachDetection(List<StorageCheckTask> tasks) {
        attachTasks.addAll(tasks);
    }

    public void onStorageAttachDetection(StorageCheckTask task) {
        attachTasks.add(task);
    }

    public void onStorageDetachDetection(List<StorageCheckTask> tasks) {
        detachTasks.addAll(tasks);
    }

    public void onStorageDetachDetection(StorageCheckTask task) {
        detachTasks.add(task);
    }

    public void start() {
        StorageCheckTask storageCheck = new StorageCheckTask();
        service.scheduleWithFixedDelay(storageCheck, 0, 5, SECONDS);
    }

    public void stop() {
        service.shutdown();
    }

}
