package com.noe.hypercube.domain;

import com.noe.hypercube.Action;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.FileEventHandler;
import com.noe.hypercube.event.domain.request.*;
import com.noe.hypercube.event.domain.response.FileListResponse;
import com.noe.hypercube.event.dto.RemoteQuotaInfo;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.persistence.FileEntityFactory;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.Client;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.Downloader;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import com.noe.hypercube.synchronization.upstream.IUploader;
import com.noe.hypercube.synchronization.upstream.QueueUploader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import net.engio.mbassy.listener.Handler;

public class AccountBox<ACCOUNT_TYPE extends Account, CLIENT, ENTITY_TYPE extends FileEntity, MAPPING_TYPE extends MappingEntity> implements FileEventHandler {

    private final Client<ACCOUNT_TYPE, CLIENT, ENTITY_TYPE, MAPPING_TYPE> client;
    private final IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper;
    private final FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory;

    private final IDownloader downloader;
    private final IUploader<ACCOUNT_TYPE, ENTITY_TYPE> uploader;

    public AccountBox(Client<ACCOUNT_TYPE, CLIENT, ENTITY_TYPE, MAPPING_TYPE> client, IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory, IPersistenceController persistenceController) {
        validate(client, mapper, entityFactory);
        this.entityFactory = entityFactory;
        this.client = client;
        this.mapper = mapper;

        downloader = new Downloader(client, mapper, entityFactory, persistenceController);
        uploader = new QueueUploader<>(client, entityFactory, persistenceController);

        if (client.isConnected()) {
            subscribeForFileEvents();
        }
        manageSubscriptions();
    }

    public void manageSubscriptions() {
        client.connectedProperty().addListener((observableValue, oldValue, connected) -> {
            if (connected) {
                subscribeForFileEvents();
            } else {
                unsubscribeFromFileEvents();
            }
        });
    }

    private void subscribeForFileEvents() {
        EventBus.subscribeToFileListRequest(this);
        EventBus.subscribeToUploadRequest(this);
        EventBus.subscribeToDownloadRequest(this);
        EventBus.subscribeToCreateFolderRequest(this);
        EventBus.subscribeToDeleteRequest(this);
    }

    private void unsubscribeFromFileEvents() {
        EventBus.unsubscribeToFileListRequest(AccountBox.this);
        EventBus.unsubscribeToUploadRequest(AccountBox.this);
        EventBus.unsubscribeToDownloadRequest(AccountBox.this);
        EventBus.unsubscribeToCreateFolderRequest(AccountBox.this);
        EventBus.unsubscribeToDeleteRequest(AccountBox.this);
    }

    private void validate(IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_TYPE> client, IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory) {
        // TODO validate types
    }

    public Class<ACCOUNT_TYPE> getAccountType() {
        return client.getAccountType();
    }

    public IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_TYPE> getClient() {
        return client;
    }

    public IMapper<ACCOUNT_TYPE, MAPPING_TYPE> getMapper() {
        return mapper;
    }

    public FileEntityFactory getEntityFactory() {
        return entityFactory;
    }

    public IDownloader getDownloader() {
        return downloader;
    }

    public IUploader<ACCOUNT_TYPE, ENTITY_TYPE> getUploader() {
        return uploader;
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onFileListRequest(final FileListRequest event) {
        if (event.getAccount().equals(client.getAccountName())) {
            try {
                final List<ServerEntry> fileList;
                final Path remoteFolder = event.getFolder();
                if (remoteFolder == null || remoteFolder.toString().isEmpty() || remoteFolder.equals(Paths.get(event.getAccount()))) {
                    fileList = client.getRootFileList();
                } else {
                    fileList = client.getFileList(normalizeToRequest(remoteFolder));
                }
                EventBus.publish(new FileListResponse(event.getTarget(), client.getAccountName(), event.getPreviousFolder(), remoteFolder, fileList, getRemoteQuotaInfo()));
            } catch (SynchronizationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onUploadRequest(final UploadRequest event) {
        if (event.getAccount().equals(client.getAccountName())) {
            try {
                // TODO uploadrequest type??? always ADDED?
                uploader.uploadNew(new UploadEntity(event.getLocalFile().toFile(), event.getRemoteFolder(), Action.ADDED));
            } catch (SynchronizationException e) {
                //TODO send fail message
            }
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onDownloadRequest(final DownloadRequest event) {
        if (event.getAccount().equals(client.getAccountName())) {
            downloader.download(new FileServerEntry(event.getAccount(), event.getRemoteFile().toString()));
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onCreateFolderRequest(final CreateFolderRequest event) {
        try {
            final Path remoteFolder = event.getBaseFolder();
            final Path folder = Paths.get(remoteFolder.toString(), event.getFolderName());
            client.createFolder(folder);
            // TODO - event published before action was done - somehow detect when action has ended
            EventBus.publish(new FileListResponse(event.getTarget(), client.getAccountName(), null, remoteFolder, client.getFileList(remoteFolder), getRemoteQuotaInfo()));
        } catch (SynchronizationException e) {
            //TODO send fail message
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onDeleteRequest(final DeleteRequest event) {
        if (event.getAccount().equals(client.getAccountName())) {
            try {
                if (event.getId() != null) {
                    client.delete(event.getId());
                } else {
                    client.delete(event.getPath());
                }
                // TODO - event published before action was done - somehow detect when action has ended
                EventBus.publish(new FileListResponse(-1, client.getAccountName(), null, event.getContainingFolder(), client.getFileList(event.getContainingFolder()), getRemoteQuotaInfo()));
            } catch (SynchronizationException e) {
                //TODO send fail message
            }
        }
    }

    private Path normalizeToRequest(final Path remoteFolder) {
        if (remoteFolder.startsWith("/")) {
            return remoteFolder;
        }
        return Paths.get("/" + remoteFolder);
    }

    private RemoteQuotaInfo getRemoteQuotaInfo() throws SynchronizationException {
        final AccountQuota quota = client.getQuota();
        return new RemoteQuotaInfo(quota.getTotalSpace(), quota.getUsedSpace());
    }
}
