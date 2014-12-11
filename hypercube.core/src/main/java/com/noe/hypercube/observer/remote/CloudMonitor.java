package com.noe.hypercube.observer.remote;

import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.service.Account;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
@Named
public class CloudMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(CloudMonitor.class);

    private final Long pollInterval;
    private ScheduledExecutorService executorService;
    private Map<Class<? extends Account>, CloudObserver> cloudObservers;

    public CloudMonitor(final Long pollInterval, final Collection<CloudObserver> cloudObservers) {
        this.pollInterval = pollInterval;
        this.cloudObservers = new HashMap<>();
        for (CloudObserver cloudObserver : cloudObservers) {
            this.cloudObservers.put(cloudObserver.getAccountType(), cloudObserver);
        }
    }

    public CloudMonitor(final Long pollInterval) {
        this.pollInterval = pollInterval;
        cloudObservers = new HashMap<>();
    }

    public void start() {
        if (cloudObservers.isEmpty()) {
            LOG.debug("No clients are added for synchronization");
        }
        executorService = Executors.newScheduledThreadPool(cloudObservers.size());
        cloudObservers.values().stream().filter(CloudObserver::isActive).forEach(observer -> {
            submit(observer);
            LOG.info("Cloud observer has been started for {}", observer.getAccountType().getName());
        });
    }

    public void submit(final ICloudObserver observer) {
        executorService.schedule(observer, pollInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        cloudObservers.values().forEach(com.noe.hypercube.observer.remote.CloudObserver::stop);
        executorService.shutdown();
        LOG.info("Cloud monitoring has been stopped");
    }

    public void addObservers(final Collection<CloudObserver> observers) {
        observers.forEach(this::addObserver);
    }

    public void addObserver(final CloudObserver observer) {
        final Class accountType = observer.getAccountType();
        if(!cloudObservers.containsKey(accountType)) {
            cloudObservers.put(accountType, observer);
            LOG.debug("Cloud observer added for account: {}", observer.getAccountName());
        } else {
            LOG.debug("Cloud observer already set for account: {}", observer.getAccountName());
        }
    }

    public void removeObserver(Class<? extends Account> accountClass, Path targetFolder) {
        cloudObservers.get(accountClass).removeTargetFolder(targetFolder);
    }

    public void addTargetFolder(final AccountBox accountBox, final Path targetFolder) {
        CloudObserver cloudObserver = cloudObservers.get(accountBox);
        if(cloudObserver == null) {
            cloudObserver = new CloudObserver(accountBox, new ArrayList<>());
            addObserver(cloudObserver);
        }
        cloudObserver.addTargetFolder(targetFolder);
        LOG.debug("New folder added for observation to {} - targetfolder: {}", accountBox.getClient().getAccountName(), targetFolder);
    }

    public CloudObserver getCloudObserver(final Class<? extends Account> accountType) {
        return cloudObservers.get(accountType);
    }

}
