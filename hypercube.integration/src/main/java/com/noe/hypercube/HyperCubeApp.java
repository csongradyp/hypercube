package com.noe.hypercube;

import com.noe.hypercube.controller.PersistenceController;
import com.noe.hypercube.synchronization.Synchronizer;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class HyperCubeApp {

    @Inject
    private Synchronizer synchronizer;
    @Inject
    private PersistenceController persistenceController;

    public void start() {
        synchronizer.start();
    }

    public void stop() {
        synchronizer.shutdown();
    }
}
