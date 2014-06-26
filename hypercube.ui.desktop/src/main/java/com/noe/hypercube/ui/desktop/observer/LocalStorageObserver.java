package com.noe.hypercube.ui.desktop.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LocalStorageObserver {

    private final ScheduledExecutorService service;
    private final List<StorageEventTask> attachTasks;
    private final List<StorageEventTask> detachTasks;

    public LocalStorageObserver() {
        service = Executors.newSingleThreadScheduledExecutor();
        attachTasks = new CopyOnWriteArrayList<>();
        detachTasks = new CopyOnWriteArrayList<>();
    }

    public void onStorageAttachDetection(List<StorageEventTask> tasks) {
        attachTasks.addAll(tasks);
    }

    public void onStorageAttachDetection(StorageEventTask task) {
        attachTasks.add(task);
    }

    public void onStorageDetachDetection(List<StorageEventTask> tasks) {
        detachTasks.addAll(tasks);
    }

    public void onStorageDetachDetection(StorageEventTask task) {
        detachTasks.add(task);
    }

    public void start() {
        StorageCheckTask storageCheck = new StorageCheckTask(attachTasks, detachTasks);
        service.scheduleWithFixedDelay(storageCheck, 0, 5, SECONDS);
    }

    public void stop() {
        service.shutdown();
    }

}
