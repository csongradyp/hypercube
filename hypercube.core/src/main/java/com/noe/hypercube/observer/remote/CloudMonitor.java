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
    private Collection<CloudObserver> observers;
    private Long pollInterval;

    public CloudMonitor(Long pollInterval, Collection<CloudObserver> observers) {
        this.pollInterval = pollInterval;
        this.observers = observers;
    }

    public CloudMonitor(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void start() {
        addObservers(observers);
        executorService = Executors.newScheduledThreadPool(observers.size());
        for (CloudObserver observer : observers) {
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
        for (CloudObserver observer : observers) {
            addObserver(observer);
        }
    }

    public void addObserver(CloudObserver observer) {
        observers.add(observer);
        LOG.debug("Cloud observer added for account: {}", observer.getAccountType().getName());
    }
}
