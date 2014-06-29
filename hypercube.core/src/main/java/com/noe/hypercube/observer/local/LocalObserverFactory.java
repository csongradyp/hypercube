package com.noe.hypercube.observer.local;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.presynchronization.LocalFilePreSynchronizer;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Named
public class LocalObserverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LocalObserverFactory.class);

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public List<LocalFileObserver> create() {
        final List<LocalFileObserver> observers = new LinkedList<>();
        final Collection<MappingEntity> mappings = persistenceController.getAllMappings();
        if(mappings != null) {
            for (MappingEntity entity : mappings) {
                final LocalFileObserver observer = createObserver(entity);
                LOG.info("File observer created for {} to watch '{}' directory ", entity.getAccountType(), entity.getLocalDir());
                observers.add(observer);
            }
        }
        return observers;
    }

    private LocalFileObserver createObserver(final MappingEntity entity) {
        final Class<? extends Account> accountType = entity.getAccountType();
        final AccountBox accountBox = accountController.getAccountBox(accountType);

        final IUploader uploader = accountBox.getUploader();
        final IMapper mapper = accountBox.getMapper();
        validate(accountType, uploader, mapper);

        final Path localDir = Paths.get(entity.getLocalDir());
        final LocalFileListener listener = new LocalFileListener(uploader, mapper);
        final LocalFilePreSynchronizer preSynchronizer = new LocalFilePreSynchronizer(listener, persistenceController.getAll(uploader.getEntityType()));
        return new LocalFileObserver(localDir, listener, preSynchronizer);
    }

    private void validate(Class<? extends Account> accountType, IUploader uploader, IMapper mapper) {
        if(uploader == null || mapper == null) {
            throw new IllegalStateException("Observer creation failed for " + accountType.getSimpleName());
        }
    }
}
