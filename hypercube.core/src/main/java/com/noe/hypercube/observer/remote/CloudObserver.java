package com.noe.hypercube.observer.remote;

import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class CloudObserver<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements ICloudObserver {

    private static final Logger LOG = LoggerFactory.getLogger(CloudObserver.class);

    private final IClient<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> client;
    private final Collection<Path> targetFolders;
    private final IDownloader downloader;

    protected CloudObserver(AccountBox<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> accountBox, final Collection<Path> targetFolders) {
        this.targetFolders = Collections.synchronizedCollection(targetFolders);
        this.downloader = accountBox.getDownloader();
        this.client = accountBox.getClient();
    }

    protected Class<ACCOUNT_TYPE> getAccountType() {
        return client.getAccountType();
    }

    public void addTargetFolder(Path targetFolder) {
        targetFolders.add(targetFolder);
    }

    public void removeTargetFolder(Path targetFolder) {
        targetFolders.remove(targetFolder);
    }

    @Override
    public void run() {
        try {
            Collection<ServerEntry> deltas = client.getChanges();
            if (deltas != null && !deltas.isEmpty()) {
                LOG.debug("Detected {} changes to process on {}", deltas.size(), client.getAccountName());
                for (ServerEntry deltaEntry : deltas) {
                    if (isRelevant(deltaEntry)) {
                        LOG.debug("Relevant content found: {}", deltaEntry);
                        downloader.download(deltaEntry);
                    }
                }
            }
        } catch (SynchronizationException e) {
            LOG.error("Error occurred while synchronize with " + client.getAccountName(), e);
        }
    }

    private boolean isRelevant(final ServerEntry entry) {
        for (Path targetFolder : targetFolders) {
            if (overlaps(entry, targetFolder)) {
                return true;
            }
        }
        return false;
    }

    private boolean overlaps(final ServerEntry entry, final Path remoteDir) {
        final String path = entry.getPath().toString();
        final String remotePath = remoteDir.toString();
        return path.contains(remotePath);
    }

    @Override
    public void stop() {
        downloader.stop();
    }

    public IDownloader getDownloader() {
        return downloader;
    }

    @Override
    public Boolean isActive() {
        return client.isConnected();
    }
}
