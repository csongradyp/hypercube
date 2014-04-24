package com.noe.hypercube.observer;


import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.List;

@Named
public class LocalFileMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileMonitor.class);

    private final FileAlterationMonitor fileMonitor;
    private List<FileAlterationObserver> observers;

    public LocalFileMonitor(long pollInterval, List<FileAlterationObserver> observers) {
        this(pollInterval);
        this.observers = observers;
        addObservers(observers);
    }

    public LocalFileMonitor(long pollInterval) {
        fileMonitor = new FileAlterationMonitor(pollInterval);
    }

    public LocalFileMonitor() {
        this(1000);
    }

    public void addObservers(List<FileAlterationObserver> observers) {
        for (FileAlterationObserver observer : observers) {
            fileMonitor.addObserver(observer);
        }
    }

    public void addObserver(FileAlterationObserver observer) {
        fileMonitor.addObserver(observer);
    }

    public void start() {
        try {
            fileMonitor.start();
        } catch (Exception e) {
            LOG.error("Failed to start File monitoring", e);
        }
    }

    public void stop() {
        try {
            fileMonitor.stop();
        } catch (Exception e) {
            LOG.error("Failed to stop File monitoring", e);
        }
    }

    public List<FileAlterationObserver> getObservers() {
        return observers;
    }
}
