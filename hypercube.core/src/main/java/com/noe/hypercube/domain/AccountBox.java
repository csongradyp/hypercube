package com.noe.hypercube.domain;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.downstream.Downloader;
import com.noe.hypercube.synchronization.downstream.IDownloader;
import com.noe.hypercube.synchronization.upstream.IUploader;
import com.noe.hypercube.synchronization.upstream.QueueUploader;

public class AccountBox<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_TYPE extends MappingEntity> {

    private final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
    private final IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper;
    private FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory;

    private IDownloader downloader;
    private IUploader<ACCOUNT_TYPE, ENTITY_TYPE> uploader;

    public AccountBox(IClient<ACCOUNT_TYPE, ENTITY_TYPE> client, IMapper<ACCOUNT_TYPE, MAPPING_TYPE> mapper, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory, IPersistenceController persistenceController) {
        validate(client, mapper, entityFactory);
        this.entityFactory = entityFactory;
        this.client = client;
        this.mapper = mapper;

        downloader = new Downloader(client, mapper, entityFactory, persistenceController);
        uploader = new QueueUploader<>(client,entityFactory, persistenceController);
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
}
