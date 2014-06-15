package com.noe.hypercube;

import com.noe.hypercube.controller.PersistenceController;
import com.noe.hypercube.domain.DbxMapping;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.synchronization.Synchronizer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

@Named
public class HyperCubeApp {

    @Inject
    private Synchronizer synchronizer;
    @Inject
    private PersistenceController persistenceController;

    public HyperCubeApp() {
    }

    public HyperCubeApp(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void start() {
        Collection<MappingEntity> mappings = persistenceController.getMappings(DbxMapping.class);
        synchronizer.start();
    }

    public void stop() {
        synchronizer.shutdown();
    }

    public void test(){
        DbxMapping testMspping = new DbxMapping("d:\\hyper\\", "/newtest");
        persistenceController.addMapping(testMspping);
    }
}
