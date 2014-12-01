package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.FileEntityFactory;
import com.noe.hypercube.service.GoogleDrive;

import java.util.Date;

public class DriveFileEntityFactory implements FileEntityFactory<GoogleDrive, DriveFileEntity> {

    @Override
    public FileEntity createFileEntity(final String localPath, final String remotePath, final String revision, final Date date) {
        return new DriveFileEntity(localPath, remotePath, revision, date);
    }

    @Override
    public Class<GoogleDrive> getAccountType() {
        return GoogleDrive.class;
    }

    @Override
    public Class<DriveFileEntity> getEntityType() {
        return DriveFileEntity.class;
    }
}
