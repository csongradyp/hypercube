package com.noe.hypercube.domain;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.FileEventHandler;
import com.noe.hypercube.event.domain.*;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.downstream.Downloader;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import com.noe.hypercube.synchronization.upstream.IUploader;
import com.noe.hypercube.synchronization.upstream.QueueUploader;
import net.engio.mbassy.listener.Handler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AccountBox<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_TYPE extends MappingEntity> implements FileEventHandler {

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
        uploader = new QueueUploader<>(client, entityFactory, persistenceController);
        EventBus.subscribeToFileListRequest(this);
        EventBus.subscribeToUploadRequest(this);
        EventBus.subscribeToDownloadRequest(this);
        EventBus.subscribeToCreateFolderRequest(this);
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
    @Handler(rejectSubtypes = true)
    public void onFileListRequest(final FileListRequest event) {
        try {
            final List<ServerEntry> fileList;
            final Path remoteFolder = event.getRemoteFolder();
            if (remoteFolder == null || remoteFolder.toString().isEmpty()) {
                fileList = client.getRootFileList();
            } else {
                fileList = client.getFileList(remoteFolder);
            }
            EventBus.publish(new FileListResponse(client.getAccountName(), event.getParentRemoteFolder(), remoteFolder, fileList));
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onUploadRequest(final UploadRequest event) {
        try {
            uploader.uploadNew(event.getLocalFile().toFile(), event.getRemoteFolder());
        } catch (SynchronizationException e) {
            // send fail message
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onDownloadRequest(final DownloadRequest event) {
        try {
            downloader.download(event.getRemoteFile(), event.getLocalFolder());
        } catch (SynchronizationException e) {
            // send fail message
        }
    }

    @Override
    @Handler
    public void onCreateFolderRequest(final CreateFolderRequest event) {
        try {
            final Path remoteFolder = event.getBaseFolder();
            final Path folder = Paths.get(remoteFolder.toString(), event.getFolderName());
            client.createFolder(folder);
            EventBus.publish(new FileListResponse(client.getAccountName(), remoteFolder, client.getFileList(remoteFolder)) );
        } catch (SynchronizationException e) {
            // send fail message
        }
    }
}
