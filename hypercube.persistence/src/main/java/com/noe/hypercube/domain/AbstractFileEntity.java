package com.noe.hypercube.domain;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public abstract class AbstractFileEntity implements FileEntity {

    @Id
    private String localPath;
    private String revision;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    public AbstractFileEntity() {}

    public AbstractFileEntity(String localPath, String revision) {
        this(localPath, revision, new Date());
    }

    public AbstractFileEntity(String localPath, String revision, Date lastModified) {
        this.localPath = localPath;
        this.revision = revision;
        this.lastModifiedDate = lastModified;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public void setLocalPath(String dbxPath) {
        this.localPath = dbxPath;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public Date lastModified() {
        return lastModifiedDate;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModifiedDate = lastModified;
    }

    @Override
    public String getId() {
        return localPath;
    }

    @Override
    public int compareTo(FileEntity entry) {
        if(localPath.equals(entry.getLocalPath())) {
            return 0;
        }
        return -1;
    }
}
