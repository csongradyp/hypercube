package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.service.GoogleDrive;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class DriveFileEntity extends AbstractFileEntity {

    private String fileId;

    public DriveFileEntity() {
    }

    public DriveFileEntity(final String localPath, final String remotePath, final String revision, final Date lastModifiedDate) {
        super(localPath, remotePath, revision, lastModifiedDate);
    }

    public DriveFileEntity(final String localPath, final String remotePath, final String revision) {
        super(localPath, remotePath, revision);
    }

    public DriveFileEntity(AbstractFileEntity fileEntity, String fileId) {
        super(fileEntity);
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(final String fileId) {
        this.fileId = fileId;
    }

    @Override
    public FileEntity getNewInstance(AbstractFileEntity fileEntity) {
        return new DriveFileEntity(fileEntity, fileId);
    }

    @Override
    public String getAccountName() {
        return GoogleDrive.name;
    }
}
