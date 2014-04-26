package com.noe.hypercube.synchronization;

import com.noe.hypercube.observer.LocalFileMonitor;
import com.noe.hypercube.observer.ObserverFactory;
import org.apache.commons.io.monitor.FileAlterationObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Named
public class Synchronizer {

    private static final int DELAY = 1;

    @Inject
    private LocalFileMonitor fileMonitor;
    @Inject
    private ObserverFactory observerFactory;

    private ScheduledExecutorService downExecutor;
    private ExecutorService upExecutor;

    private List<Runnable> downloaders;
    private List<Runnable> uploaders;

    public Synchronizer(final List<Runnable> downloaders, final List<Runnable> uploaders) {
        this.downloaders = downloaders;
        this.uploaders = uploaders;
    }

    @PostConstruct
    public void createExecutors() {
        createScheduledExecutor(downloaders);
        createExecutor(uploaders);
    }

    private void createExecutor(List<Runnable> uploaders) {
        if(!uploaders.isEmpty()) {
            upExecutor = Executors.newFixedThreadPool(uploaders.size());
        }
    }

    private void createScheduledExecutor(List<Runnable> downloaders) {
        if(!downloaders.isEmpty()) {
            downExecutor = Executors.newScheduledThreadPool(downloaders.size());
        }
    }

    public void start() {
        List<FileAlterationObserver> observers = observerFactory.create();

        fileMonitor.addObservers(observers);
        fileMonitor.start();
        submitDownloads();
        submitUploads();
    }

    private void submitDownloads() {
        for (Runnable task : downloaders) {
            downExecutor.scheduleWithFixedDelay(task, 0, DELAY, SECONDS);
        }
    }

    private void submitUploads() {
        for (Runnable task : uploaders) {
            upExecutor.execute(task);
        }
    }

    public void shutdown() {
        downExecutor.shutdown();
    }

    public void setDownloaders(List<Runnable> downloaders) {
        this.downloaders = downloaders;
    }
}
