package com.noe.hypercube.synchronization;

import com.noe.hypercube.observer.LocalFileMonitor;
import com.noe.hypercube.observer.ObserverFactory;
import org.apache.commons.io.monitor.FileAlterationObserver;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Named
public class Synchronizer {

    private static final int DELAY = 1;

    @Inject
    private LocalFileMonitor fileMonitor;
    @Inject
    private ObserverFactory observerFactory;
    @Inject
    private ScheduledExecutorService executorService;

    private List<Runnable> downstreamTasks;

    public Synchronizer(List<Runnable> downstreamTasks) {
        this.downstreamTasks = downstreamTasks;
    }

    public void start() {
        List<FileAlterationObserver> observers = observerFactory.create();

        fileMonitor.addObservers(observers);
        fileMonitor.start();
        for (Runnable task : downstreamTasks) {
            executorService.scheduleWithFixedDelay(task, 0, DELAY, SECONDS);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void setDownstreamTasks(List<Runnable> downstreamTasks) {
        this.downstreamTasks = downstreamTasks;
    }
}
