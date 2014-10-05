package com.noe.hypercube.domain;

import java.util.Date;

public class TestEntity implements FileEntity {

    @Override
    public String getLocalPath() {
        return "A/B";
    }

    @Override
    public String getRemotePath() {
        return "";
    }

    @Override
    public void setRemotePath(String remotePath) {
    }

    @Override
    public String getRevision() {
        return "1";
    }

    @Override
    public void setRevision(String revision) {
    }

    @Override
    public Date lastModified() {
        return new Date();
    }

    @Override
    public void setLastModified(Date lastModified) {
    }

    @Override
    public String getAccountName() {
        return null;
    }

    @Override
    public int compareTo(FileEntity o) {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }
}
