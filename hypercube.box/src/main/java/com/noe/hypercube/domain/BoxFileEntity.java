package com.noe.hypercube.domain;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class BoxFileEntity extends AbstractFileEntity {

    public BoxFileEntity() {
        super();
    }

    public BoxFileEntity(final String localPath, final String revision) {
        super(localPath, revision);
    }

    public BoxFileEntity(final String localPath, final String revision, final Date lastModified) {
        super(localPath, revision, lastModified);
    }
}
