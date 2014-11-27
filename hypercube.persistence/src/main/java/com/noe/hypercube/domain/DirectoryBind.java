package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.IEntity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DirectoryBind implements IEntity {

    @Id
    private String localDir;
    private String remoteDir;

    public DirectoryBind() {
    }

    public DirectoryBind(String localDir, String remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
    }

    public String getLocalDir() {
        return localDir;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @Override
    public String getId() {
        return localDir;
    }

}
