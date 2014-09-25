package com.noe.hypercube.domain;

import com.noe.hypercube.service.Dropbox;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class DbxFileEntity extends AbstractFileEntity {

    public DbxFileEntity() {
        super();
    }

    public DbxFileEntity(String localPath, String remotePath, String revision, Date lastModifiedDate) {
        super(localPath, remotePath, revision, lastModifiedDate);
    }

    public DbxFileEntity(String localPath, String remotePath, String revision) {
        super(localPath, remotePath, revision);
    }

    @Override
    public String getAccountName() {
        return Dropbox.getName();
    }
}
