package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.FileEntity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public abstract class AbstractFileEntity implements FileEntity {

    @Id
    private String localPath;
    private String remotePath;
    private String revision;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    public AbstractFileEntity() {
    }

    protected AbstractFileEntity(final String localPath, final String remotePath, final String revision, final Date lastModifiedDate) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.revision = revision;
        this.lastModifiedDate = lastModifiedDate;
    }

    protected AbstractFileEntity(final String localPath, final String remotePath, final String revision) {
        this(localPath, remotePath, revision, new Date());
    }

    public AbstractFileEntity(final AbstractFileEntity fileEntity) {
        this(fileEntity.getLocalPath(), fileEntity.getRemotePath(), fileEntity.getRevision(), fileEntity.lastModified());
    }

    @Override
    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
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
    public String getRemotePath() {
        return remotePath;
    }

    @Override
    public int compareTo(FileEntity entry) {
        if (localPath.equals(entry.getLocalPath())) {
            return 0;
        }
        return -1;
    }

    @Override
    public FileEntity duplicate() {
        return getNewInstance(this);
    }

    public abstract FileEntity getNewInstance(AbstractFileEntity fileEntity);
}
