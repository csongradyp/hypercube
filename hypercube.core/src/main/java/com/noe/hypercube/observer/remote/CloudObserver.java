package com.noe.hypercube.observer.remote;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class CloudObserver<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CloudObserver.class);

    private final IPersistenceController persistenceController;
    private final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
    private final IMapper<ACCOUNT_TYPE, ? extends MappingEntity> directoryMapper;
    private final IDownloader downloader;

    protected CloudObserver(AccountBox<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> accountBox, IPersistenceController persistenceController) {
        this.persistenceController = persistenceController;
        this.downloader = accountBox.createDownloader(persistenceController);
        this.client = accountBox.getClient();
        this.directoryMapper = accountBox.getMapper();
    }

    protected Class<ACCOUNT_TYPE> getAccountType() {
        return client.getAccountType();
    }

    @Override
    public void run() {
        try {
            Collection<ServerEntry> deltas = client.getChanges();
            if (deltas != null && !deltas.isEmpty()) {
                LOG.debug("Detected {} changes to process on {}", deltas.size(), client.getAccountName());
                for (ServerEntry deltaEntry : deltas) {
                    if(isMapped(deltaEntry)) {
                        LOG.debug("Mapped content found: {}", deltaEntry);
                        downloader.download(deltaEntry);
                    }
                }
            }
        } catch (SynchronizationException e) {
            LOG.error("Error occurred while synchronize with " + client.getAccountName(), e);
        }
    }

    private boolean isMapped(ServerEntry entry) {
        Collection<MappingEntity> mappings = persistenceController.getMappings(directoryMapper.getMappingClass());
        for (MappingEntity mapping : mappings) {
            if(overlaps(entry, Paths.get(mapping.getRemoteDir()))) {
                return true;
            }
        }
        return false;
    }

    private boolean overlaps(ServerEntry entry, Path remoteDir) {
        return entry.getPath().toString().contains(remoteDir.toString().toLowerCase());
    }

    public IDownloader getDownloader() {
        return downloader;
    }
}
