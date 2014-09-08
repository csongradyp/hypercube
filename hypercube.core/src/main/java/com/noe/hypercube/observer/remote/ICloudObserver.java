package com.noe.hypercube.observer.remote;

public interface ICloudObserver extends Runnable {

    void stop();

    Boolean isActive();
}
