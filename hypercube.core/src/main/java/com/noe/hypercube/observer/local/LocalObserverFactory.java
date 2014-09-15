package com.noe.hypercube.observer.local;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Named
public class LocalObserverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LocalObserverFactory.class);

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public List<LocalFileObserver> create() {
        final List<LocalFileObserver> observers = new ArrayList<>();
        final Collection<String> localMappings = persistenceController.getLocalMappings();
        for (String localFolder : localMappings) {
            final Path localDirectory = Paths.get(localFolder);
            final Collection<LocalFileObserver> fileObservers = createObserversFor(localDirectory);
            observers.addAll(fileObservers);
        }
        return observers;
    }

    private Collection<LocalFileObserver> createObserversFor(final Path localDir) {
        final Collection<LocalFileObserver> fileObservers = new ArrayList<>();
        fileObservers.add(createFileObserver(localDir));
        return fileObservers;
    }

    private LocalFileObserver createFileObserver(final Path localDir) {
        final FileAlterationListener listener = new LocalFileListener(localDir, accountController.getAll(), persistenceController);
        final LocalFileObserver localFileObserver = new LocalFileObserver(localDir, listener);
        LOG.info("File observer created to watch '{}' directory ", localDir);
        return localFileObserver;
    }
}