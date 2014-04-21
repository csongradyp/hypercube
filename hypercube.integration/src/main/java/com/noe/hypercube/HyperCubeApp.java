package com.noe.hypercube;


import com.noe.hypercube.synchronization.Synchronizer;

public class HyperCubeApp {

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
