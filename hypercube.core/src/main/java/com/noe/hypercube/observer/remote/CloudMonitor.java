package com.noe.hypercube.observer.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Named
public class CloudMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(CloudMonitor.class);

    private ScheduledExecutorService executorService;
    private Collection<CloudObserver> cloudObservers;
    private final Long pollInterval;

    public CloudMonitor(Long pollInterval, Collection<CloudObserver> cloudObservers) {
        this.pollInterval = pollInterval;
        this.cloudObservers = cloudObservers;
    }

    public CloudMonitor(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void start() {
        if(cloudObservers.isEmpty()) {
            LOG.error("No clients are added for synchronization");
        }
        executorService = Executors.newScheduledThreadPool(cloudObservers.size());
        for (CloudObserver observer : cloudObservers) {
            submit(observer);
            LOG.info("Cloud observer has been started for {}", observer.getAccountType().getName());
        }
    }

    public void submit(CloudObserver observer) {
        executorService.schedule(observer, pollInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
        LOG.info("Cloud monitoring has been stopped");
    }

    public void addObservers(Collection<CloudObserver> observers) {
        if(cloudObservers == null || observers.isEmpty()) {
            cloudObservers = observers;
        }
        else  {
            for (CloudObserver observer : observers) {
                addObserver(observer);
            }
        }
    }

    public void addObserver(CloudObserver observer) {
        cloudObservers.add(observer);
        LOG.debug("Cloud observer added for account: {}", observer.getAccountType().getName());
    }
}
