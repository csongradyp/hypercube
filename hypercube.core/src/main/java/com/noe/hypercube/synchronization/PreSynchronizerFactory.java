package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.synchronization.presynchronization.IPreSynchronizer;
import com.noe.hypercube.synchronization.presynchronization.ManagedFolderPreSynchronizer;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named
public class PreSynchronizerFactory {

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public Collection<IPreSynchronizer> create(final List<LocalFileObserver> localObservers) {
        final Collection<IPreSynchronizer> preSynchronizers = new ArrayList<>();
        for (LocalFileObserver localObserver : localObservers) {
            final Path localFolder = localObserver.getTargetDir();
            preSynchronizers.add(new ManagedFolderPreSynchronizer(localFolder, persistenceController, accountController));
        }
        return preSynchronizers;
    }
}