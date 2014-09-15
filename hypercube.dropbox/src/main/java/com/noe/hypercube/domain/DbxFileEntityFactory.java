package com.noe.hypercube.domain;

import com.noe.hypercube.service.Dropbox;

import java.util.Date;

public class DbxFileEntityFactory implements FileEntityFactory<Dropbox, DbxFileEntity> {

    @Override
    public DbxFileEntity createFileEntity(String localPath, String remotePath, String revision, Date lastModified) {
        return new DbxFileEntity(localPath, remotePath, revision, lastModified);
    }

    @Override
    public Class<Dropbox> getAccountType() {
        return Dropbox.class;
    }

    @Override
    public Class<DbxFileEntity> getEntityType() {
        return DbxFileEntity.class;
    }
}
