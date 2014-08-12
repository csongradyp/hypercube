package com.noe.hypercube.domain;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileListRequest;
import com.noe.hypercube.event.domain.FileListResponse;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.Downloader;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import com.noe.hypercube.synchronization.upstream.IUploader;
import com.noe.hypercube.synchronization.upstream.QueueUploader;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

import java.nio.file.Path;
import java.util.List;

public class AccountBox<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_TYPE extends MappingEntity> implements EventHandler<FileListRequest> {

    private final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
    private final IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper;
    private final FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory;

    private final IDownloader downloader;
    private final IUploader<ACCOUNT_TYPE, ENTITY_TYPE> uploader;

    public AccountBox(IClient<ACCOUNT_TYPE, ENTITY_TYPE> client, IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory, IPersistenceController persistenceController) {
        validate(client, mapper, entityFactory);
        this.entityFactory = entityFactory;
        this.client = client;
        this.mapper = mapper;

        downloader = new Downloader(client, mapper, entityFactory, persistenceController);
        uploader = new QueueUploader<>(client,entityFactory, persistenceController);
        EventBus.subscribeToFileListRequest(this);
    }

    private void validate(IClient<ACCOUNT_TYPE, ENTITY_TYPE> client, IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory) {
        // TODO validate types
    }

    public Class<ACCOUNT_TYPE> getAccountType() {
        return client.getAccountType();
    }

    public IClient<ACCOUNT_TYPE, ENTITY_TYPE> getClient() {
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
    @Handler(rejectSubtypes = true, delivery = Invoke.Synchronously)
    public void onEvent(final FileListRequest event) {
        try {
            final List<ServerEntry> fileList;
            final Path remoteFolder = event.getRemoteFolder();
            if (remoteFolder == null || remoteFolder.toString().isEmpty()) {
                fileList = client.getRootFileList();
            } else {
                fileList = client.getFileList(remoteFolder);
            }
            EventBus.publish(new FileListResponse(client.getAccountName(), remoteFolder, fileList));
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }
}
