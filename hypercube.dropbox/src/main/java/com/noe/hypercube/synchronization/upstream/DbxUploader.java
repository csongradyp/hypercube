package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.service.Dropbox;
import com.noe.hypercube.service.IClient;

import java.util.Date;

public class DbxUploader extends Uploader<Dropbox, DbxFileEntity> {

    public DbxUploader(IClient client, IPersistenceController persistenceController) {
        super(client, persistenceController);
    }

    @Override
    public Class<DbxFileEntity> getEntityType() {
        return DbxFileEntity.class;
    }

    @Override
    public Class getAccountType() {
        return Dropbox.class;
    }

    @Override
    protected FileEntity createFileEntity(String localPath, String revision, Date lastModified) {
        return new DbxFileEntity(localPath, revision, lastModified);
    }
}
