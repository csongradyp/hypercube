package com.noe.hypercube.googledrive.domain;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.googledrive.service.GoogleDrive;

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
