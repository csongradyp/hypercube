package com.noe.hypercube;

import com.noe.hypercube.synchronization.Synchronizer;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class HyperCubeApp {

    @Inject
    private Synchronizer synchronizer;

    public HyperCubeApp(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void start() {
        synchronizer.start();
    }

    public void stop() {
        synchronizer.shutdown();
    }
}
