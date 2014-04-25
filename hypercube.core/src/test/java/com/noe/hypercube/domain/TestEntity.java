package com.noe.hypercube.domain;

import java.util.Date;

public class TestEntity implements  FileEntity {
    @Override
    public String getLocalPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLocalPath(String dbxPath) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRevision() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRevision(String revision) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date lastModified() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLastModified(Date lastModified) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int compareTo(FileEntity o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
