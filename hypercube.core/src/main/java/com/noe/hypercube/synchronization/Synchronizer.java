package com.noe.hypercube.synchronization;

import com.noe.hypercube.observer.LocalFileObserver;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Synchronizer {

    private static final int INITIAL_DELAY = 0;
    private static final int DELAY = 1;

    private LocalFileObserver observer;
    private ScheduledExecutorService executorService;
    private List<Runnable> tasks;

    public Synchronizer(LocalFileObserver observer, ScheduledExecutorService executorService) {
        this.observer = observer;
        this.executorService = executorService;
    }

    public Synchronizer(LocalFileObserver observer, ScheduledExecutorService executor, List<Runnable> tasks) {
        this.observer = observer;
        this.executorService = executor;
        this.tasks = tasks;
    }

    public void start() {
        observer.precheckAndStart();
        for (Runnable task : tasks) {
            executorService.scheduleWithFixedDelay(task, INITIAL_DELAY, DELAY, SECONDS);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void setTasks(List<Runnable> tasks) {
        this.tasks = tasks;
    }
}
