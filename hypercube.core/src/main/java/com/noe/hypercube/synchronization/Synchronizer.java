package com.noe.hypercube.synchronization;

import com.noe.hypercube.observer.local.LocalFileMonitor;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.observer.local.LocalObserverFactory;
import com.noe.hypercube.observer.remote.CloudMonitor;
import com.noe.hypercube.observer.remote.CloudObserver;
import com.noe.hypercube.observer.remote.CloudObserverFactory;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Named
public class Synchronizer {

    private static final Logger LOG = Logger.getLogger(Synchronizer.class);

    @Inject
    private LocalFileMonitor fileMonitor;
    @Inject
    private CloudMonitor cloudMonitor;
    @Inject
    private LocalObserverFactory localObserverFactory;
    @Inject
    private CloudObserverFactory cloudObserverFactory;

    private ExecutorService executorService;

    private List<LocalFileObserver> localObservers;
    private Collection<CloudObserver> cloudObservers;

    @PostConstruct
    public void createExecutors() {
        localObservers = localObserverFactory.create();
        cloudObservers = cloudObserverFactory.create();
        fileMonitor.addObservers(localObservers);
        cloudMonitor.addObservers(cloudObservers);
        createExecutor();
    }

    private void createExecutor() {
//        if(!(localObservers.isEmpty() && cloudObservers.isEmpty())) {
        executorService = Executors.newFixedThreadPool(localObservers.size() + cloudObservers.size());
//        }
    }

    public void start() {
        submitDownloaders();
        submitUploaders();
        cloudMonitor.start();
        fileMonitor.start();
        LOG.info("Synchronization has been fully started");
    }

    private void submitDownloaders() {
        for (CloudObserver observer : cloudObservers) {
            executorService.submit(observer.getDownloader());
        }
    }

    private void submitUploaders() {
        for (LocalFileObserver observer : localObservers) {
            executorService.submit(observer.getUploader());
        }
    }

    public void shutdown() {
        executorService.shutdown();
        LOG.info("Synchronization has been shutted down");
    }

    public LocalFileMonitor getFileMonitor() {
        return fileMonitor;
    }

    public CloudMonitor getCloudMonitor() {
        return cloudMonitor;
    }
}
