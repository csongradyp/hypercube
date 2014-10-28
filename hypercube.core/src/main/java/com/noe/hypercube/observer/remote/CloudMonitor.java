package com.noe.hypercube.observer.remote;

import com.noe.hypercube.service.Account;
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
            LOG.error("No clients are added for synchronization");
        }
        executorService = Executors.newScheduledThreadPool(cloudObservers.size());
        for (CloudObserver observer : cloudObservers.values()) {
            if(observer.isActive()) {
                submit(observer);
                LOG.info("Cloud observer has been started for {}", observer.getAccountType().getName());
            }
        }
    }

    public void submit(final ICloudObserver observer) {
        executorService.schedule(observer, pollInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        for (CloudObserver cloudObserver : cloudObservers.values()) {
            cloudObserver.stop();
        }
        executorService.shutdown();
        LOG.info("Cloud monitoring has been stopped");
    }

    public void addObservers(final Collection<CloudObserver> observers) {
        for (CloudObserver observer : observers) {
            addObserver(observer);
        }
    }

    public void addObserver(final CloudObserver observer) {
        final Class accountType = observer.getAccountType();
        if(!cloudObservers.containsKey(accountType)) {
            cloudObservers.put(accountType, observer);
            LOG.debug("Cloud observer added for account: {}", accountType.getName());
        } else {
            LOG.debug("Cloud observer already set for account: {}", accountType.getName());
        }
    }

    public void addTargetFolder(final Class<? extends Account> accountType, final Path targetFolder) {
        cloudObservers.get(accountType).addTargetFolder(targetFolder);
        LOG.debug("New folder added for observation to {} - targetfolder: {}", accountType.getName(), targetFolder);
    }

    public CloudObserver getCloudObserver(final Class<? extends Account> accountType) {
        return cloudObservers.get(accountType);
    }

}
