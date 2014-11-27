package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.service.Box;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class BoxFileEntity extends AbstractFileEntity {

    public BoxFileEntity() {
        super();
    }

    public BoxFileEntity(final String localPath, final String remotePath, final String revision) {
        super(localPath, remotePath, revision);
    }

    public BoxFileEntity(AbstractFileEntity fileEntity) {
        super(fileEntity);
    }

    public BoxFileEntity(final String localPath, final String remotePath, final String revision, final Date lastModified) {
        super(localPath, remotePath, revision, lastModified);
    }

    @Override
    public FileEntity getNewInstance(AbstractFileEntity fileEntity) {
        return new BoxFileEntity(fileEntity);
    }

    @Override
    public String getAccountName() {
        return Box.getName();
    }
}
