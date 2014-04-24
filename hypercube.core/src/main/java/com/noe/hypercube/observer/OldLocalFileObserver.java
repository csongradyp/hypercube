package com.noe.hypercube.observer;

import com.noe.hypercube.synchronization.presynchronization.FilePreSynchronizer;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

public class OldLocalFileObserver {

    private static final Logger LOG = Logger.getLogger(OldLocalFileObserver.class);

    private FileAlterationMonitor fileMonitor;
    private FileAlterationObserver observer;
    private List<FilePreSynchronizer> preCheckers;

    public OldLocalFileObserver(FileAlterationMonitor fileMonitor, FileAlterationObserver observer, FileAlterationListener listener, List<FilePreSynchronizer> preSynchronizers) {
        this.fileMonitor = fileMonitor;
        this.observer = observer;
        this.preCheckers = preSynchronizers;
        observer.addListener(listener);
    }

    public void precheckAndStart() {
        File[] currentLocalFiles = observer.getDirectory().listFiles();
        LOG.info("PreSynchronization: looking for local files changed before start.");
        for (FilePreSynchronizer preSynchronizer : preCheckers) {
            preSynchronizer.run(currentLocalFiles);
        }
        LOG.info("PreSynchronization finished.");
        try {
            fileMonitor.start();
            LOG.info("Local file observer started.");
        } catch (Exception e) {
            LOG.error("Failed to start local file observer", e);
        }
    }


}
