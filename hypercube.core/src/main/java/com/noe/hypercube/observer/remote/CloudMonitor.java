package com.noe.hypercube.observer.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
@Named
public class CloudMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(CloudMonitor.class);

    private final Long pollInterval;
    private ScheduledExecutorService executorService;
    private Collection<CloudObserver> cloudObservers;

    public CloudMonitor(final Long pollInterval, final Collection<CloudObserver> cloudObservers) {
        this.pollInterval = pollInterval;
        this.cloudObservers = cloudObservers;
    }

    public CloudMonitor(Long pollInterval) {
        this.pollInterval = pollInterval;
        cloudObservers = new ArrayList<>();
    }

    public void start() {
        if (cloudObservers.isEmpty()) {
            LOG.error("No clients are added for synchronization");
        }
        executorService = Executors.newScheduledThreadPool(cloudObservers.size());
        for (CloudObserver observer : cloudObservers) {
            if(observer.isActive()) {
                submit(observer);
                LOG.info("Cloud observer has been started for {}", observer.getAccountType().getName());
            }
        }
    }

    public void submit(ICloudObserver observer) {
        executorService.schedule(observer, pollInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        for (CloudObserver cloudObserver : cloudObservers) {
            cloudObserver.stop();
        }
        executorService.shutdown();
        LOG.info("Cloud monitoring has been stopped");
    }

    public void addObservers(Collection<CloudObserver> observers) {
        for (CloudObserver observer : observers) {
            addObserver(observer);
        }
    }

    public void addObserver(CloudObserver observer) {
        cloudObservers.add(observer);
        LOG.debug("Cloud observer added for account: {}", observer.getAccountType().getName());
    }
}
