package com.noe.hypercube.domain;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class DbxFileEntity extends AbstractFileEntity {

    public DbxFileEntity() {
        super();
    }

    public DbxFileEntity(String localPath, String revision) {
        super(localPath, revision);
    }

    public DbxFileEntity(String localPath, String revision, Date lastModified) {
        super(localPath, revision, lastModified);
    }
}
