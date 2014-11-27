package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.FileEntity;
import java.util.Date;

public class TestEntity implements FileEntity {

    private String rev;
    private Date lastModified;
    private String remotePath;
    private String localPath;

    public TestEntity(String localPath, String remotePath, Date lastModified, String rev) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.lastModified = lastModified;
        this.rev = rev;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public String getRemotePath() {
        return remotePath;
    }

    @Override
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    @Override
    public String getRevision() {
        return rev;
    }

    @Override
    public void setRevision(String revision) {
        rev = revision;
    }

    @Override
    public Date lastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getAccountName() {
        return "test";
    }

    @Override
    public FileEntity duplicate() {
        return this;
    }

    @Override
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public int compareTo(FileEntity o) {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "rev='" + rev + '\'' +
                ", lastModified=" + lastModified +
                ", remotePath='" + remotePath + '\'' +
                ", localPath='" + localPath + '\'' +
                '}';
    }
}
