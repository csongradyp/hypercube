package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.observer.local.LocalFileMonitor;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.observer.local.LocalObserverFactory;
import com.noe.hypercube.observer.remote.CloudMonitor;
import com.noe.hypercube.observer.remote.CloudObserver;
import com.noe.hypercube.observer.remote.CloudObserverFactory;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.presynchronization.IFilePreSynchronizer;
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
    @Inject
    private PreSynchronizerFactory preSynchronizerFactory;
    @Inject
    private IAccountController accountController;

    private ExecutorService executorService;
    private List<LocalFileObserver> localObservers;
    private Collection<IFilePreSynchronizer> preSynchronizers;
    private Collection<CloudObserver<? extends Account, ? extends FileEntity>> cloudObservers;

    @PostConstruct
    public void createExecutors() {
        localObservers = localObserverFactory.create();
        // TODO create presynchronizer factory - list of presynchs - run before startup - reuse when new mapping is added
        preSynchronizers = preSynchronizerFactory.create(localObservers);
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
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            executorService.submit(accountBox.getUploader());
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
