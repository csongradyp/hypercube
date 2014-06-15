package com.noe.hypercube.domain;

import com.noe.hypercube.service.Dropbox;

import java.util.Date;

public class DbxFileEntityFactory implements FileEntityFactory<Dropbox, DbxFileEntity> {

    @Override
    public FileEntity createFileEntity(final String localPath, final String revision, final Date date) {
        return new DbxFileEntity(localPath, revision, date);
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
