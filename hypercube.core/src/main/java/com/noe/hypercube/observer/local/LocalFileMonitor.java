package com.noe.hypercube.observer.local;


import java.nio.file.Path;
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
    private List<LocalFileObserver> observers;

    public LocalFileMonitor(long pollInterval) {
        fileMonitor = new FileAlterationMonitor(pollInterval);
    }

    public LocalFileMonitor(long pollInterval, List<LocalFileObserver> observers) {
        this(pollInterval);
        this.observers = observers;
        addObservers(observers);
    }

    public LocalFileMonitor() {
        this(1000);
    }

    public void addObservers(List<LocalFileObserver> observers) {
        for (FileAlterationObserver observer : observers) {
            fileMonitor.addObserver(observer);
        }
    }

    public void addObserver(LocalFileObserver observer) {
        fileMonitor.addObserver(observer);
        LOG.info("Observer has been added to File Monitor - targetfolder: {}", observer.getTargetDir());
    }

    public void removeObserver(final Path targetDir) {
        FileAlterationObserver toRemove = null;
        final Iterable<FileAlterationObserver> observers = fileMonitor.getObservers();
        for (FileAlterationObserver fileAlterationObserver : observers) {
            if (fileAlterationObserver.getDirectory().toPath().equals(targetDir)) {
                toRemove = fileAlterationObserver;
            }
        }
        fileMonitor.removeObserver(toRemove);
    }


    public void start() {
        try {
            fileMonitor.start();
        } catch (Exception e) {
            LOG.error("Failed to start File monitoring", e);
        }
        LOG.info("Local file monitoring has been started");
    }

    public void stop() {
        try {
            fileMonitor.stop();
        } catch (Exception e) {
            LOG.error("Failed to stop File monitoring", e);
        }
        LOG.info("Local file monitoring has been stopped");
    }

    public List<LocalFileObserver> getObservers() {
        return observers;
    }
}
