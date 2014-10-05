package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.observer.local.LocalFileMonitor;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.observer.local.LocalObserverFactory;
import com.noe.hypercube.observer.remote.CloudMonitor;
import com.noe.hypercube.observer.remote.CloudObserver;
import com.noe.hypercube.observer.remote.CloudObserverFactory;
import com.noe.hypercube.synchronization.presynchronization.IPreSynchronizer;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Named
public class Synchronizer {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Synchronizer.class);

    @Inject
    private LocalFileMonitor fileMonitor;
    @Inject
    private CloudMonitor cloudMonitor;
    @Inject
    private LocalObserverFactory localObserverFactory;
    @Inject
    private CloudObserverFactory cloudObserverFactory;
    @Inject
    PreSynchronizerFactory preSynchronizerFactory;
    @Inject
    private IAccountController accountController;

    private ExecutorService executorService;

    private List<LocalFileObserver> localObservers;
    private Collection<CloudObserver> cloudObservers;
    private Collection<IPreSynchronizer> preSynchronizers;

    @PostConstruct
    public void createExecutors() {
        localObservers = localObserverFactory.create();
        cloudObservers = cloudObserverFactory.create();
        preSynchronizers = preSynchronizerFactory.create(localObservers);
        executorService = Executors.newFixedThreadPool(localObservers.size() + cloudObservers.size() + preSynchronizers.size());
    }

    public void start() {
        submitDownloaders();
        submitUploaders();
        presynchronize();
        fileMonitor.addObservers(localObservers);
        cloudMonitor.addObservers(cloudObservers);
        cloudMonitor.start();
        fileMonitor.start();
        LOG.info("Synchronization has been fully started");
    }

    private void presynchronize() {
        LOG.info("PreSynchronization has been started");
        for (IPreSynchronizer preSynchronizer : preSynchronizers) {
            executorService.submit(preSynchronizer);
            LOG.info("PreSynchronizer submitted for folder {}", preSynchronizer.getTargetFolder());
        }
    }

    private void submitDownloaders() {
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            executorService.submit(accountBox.getDownloader());
            LOG.info("Downloader submitted for account: {}", accountBox.getClient().getAccountName());
        }
    }

    private void submitUploaders() {
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            executorService.submit(accountBox.getUploader());
            LOG.info("Uploader submitted for account: {}", accountBox.getClient().getAccountName());
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
