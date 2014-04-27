package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class CloudObserver<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CloudObserver.class);

    @Inject
    private IPersistenceController persistenceController;
    private final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
    private final DirectoryMapper<ACCOUNT_TYPE, ? extends MappingEntity> directoryMapper;
    private final IDownloader<ACCOUNT_TYPE> downloader;

    protected CloudObserver(IClient<ACCOUNT_TYPE, ENTITY_TYPE> client, DirectoryMapper<ACCOUNT_TYPE, ? extends MappingEntity> directoryMapper, IDownloader<ACCOUNT_TYPE> downloader) {
        this.client = client;
        this.directoryMapper = directoryMapper;
        this.downloader = downloader;
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
}
