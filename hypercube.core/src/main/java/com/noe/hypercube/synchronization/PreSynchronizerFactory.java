package com.noe.hypercube.synchronization;

import com.noe.hypercube.observer.local.LocalFileObserver;
import com.noe.hypercube.synchronization.presynchronization.FolderPreSynchronizer;
import com.noe.hypercube.synchronization.presynchronization.IPreSynchronizer;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named
public class PreSynchronizerFactory {

    public Collection<IPreSynchronizer> create(final List<LocalFileObserver> localObservers) {
        final Collection<IPreSynchronizer> preSynchronizers = new ArrayList<>();
        for (LocalFileObserver localObserver : localObservers) {
            final Path localFolder = localObserver.getTargetDir();
            preSynchronizers.add(new FolderPreSynchronizer(localFolder));
        }
        return preSynchronizers;
    }
}