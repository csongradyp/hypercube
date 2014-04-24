package com.noe.hypercube.synchronization.downstream;


import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public abstract class CouldObserver implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CouldObserver.class);

    private final IClient client;
    private final DirectoryMapper<? extends MappingEntity, ? extends FileEntity> directoryMapper;
    private final IPersistenceController persistenceController;
    private final BlockingQueue<ServerEntry> entryQueue;

    protected CouldObserver(IClient client, DirectoryMapper<? extends MappingEntity, ? extends FileEntity> directoryMapper, IPersistenceController persistenceController, BlockingQueue<ServerEntry> entryQueue) {
        this.client = client;
        this.directoryMapper = directoryMapper;
        this.persistenceController = persistenceController;
        this.entryQueue = entryQueue;
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
                        entryQueue.add(deltaEntry);
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
