package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.synchronization.presynchronization.FolderPreSynchronizer;
import com.noe.hypercube.synchronization.presynchronization.IPreSynchronizer;
import com.noe.hypercube.synchronization.presynchronization.util.PreSynchronizationSubmitManager;

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
    @Inject
    private PreSynchronizationSubmitManager submitManager;

    public Collection<IPreSynchronizer> create(final List<LocalFileObserver> localObservers) {
        final Collection<IPreSynchronizer> preSynchronizers = new ArrayList<>();
        for (LocalFileObserver localObserver : localObservers) {
            preSynchronizers.add(create(localObserver));
        }
        return preSynchronizers;
    }

    public IPreSynchronizer create(final LocalFileObserver localObserver) {
        final Path localFolder = localObserver.getTargetDir();
        return create(localFolder);
    }

    public IPreSynchronizer create(final Path localFolder) {
        return new FolderPreSynchronizer(localFolder, persistenceController, accountController, submitManager);
    }

}