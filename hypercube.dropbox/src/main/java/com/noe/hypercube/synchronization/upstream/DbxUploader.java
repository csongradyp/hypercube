package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.service.IClient;

import java.util.Date;

public class DbxUploader extends Uploader {

    public DbxUploader(IClient client, IPersistenceController persistenceController) {
        super(client, persistenceController);
    }

    @Override
    public Class getEntityClass() {
        return DbxFileEntity.class;
    }

    @Override
    protected FileEntity createFileEntity(String localPath, String revision, Date lastModified) {
        return new DbxFileEntity(localPath, revision, lastModified);
    }
}
