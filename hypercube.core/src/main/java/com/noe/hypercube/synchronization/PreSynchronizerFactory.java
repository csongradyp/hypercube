package com.noe.hypercube.synchronization;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.observer.local.LocalFileListener;
import com.noe.hypercube.synchronization.presynchronization.FilePreSynchronizer;
import com.noe.hypercube.synchronization.presynchronization.IFilePreSynchronizer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

public class PreSynchronizerFactory {

    @Inject
    private IPersistenceController persistenceController;

    public Collection<IFilePreSynchronizer> create(final Collection<LocalFileListener> fileListeners) {
        final Collection<IFilePreSynchronizer> filePreSynchronizers = new ArrayList<>();
        for (LocalFileListener listener : fileListeners) {
            final Collection<FileEntity> mappedLocalFiles = persistenceController.getMappingUnder(listener.getTargetDir().toString());
            new FilePreSynchronizer(listener, mappedLocalFiles);
        }
        return filePreSynchronizers;
    }
}
