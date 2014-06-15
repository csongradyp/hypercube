package com.noe.hypercube.googledrive.domain;

import com.noe.hypercube.domain.AbstractFileEntity;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class DriveFileEntity extends AbstractFileEntity {

    private String fileId;

    public DriveFileEntity() {
    }

    public DriveFileEntity(String localPath, String revision, Date date) {
        super(localPath, revision, date);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
