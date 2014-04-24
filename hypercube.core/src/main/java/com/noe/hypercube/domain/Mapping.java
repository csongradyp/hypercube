package com.noe.hypercube.domain;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Mapping implements MappingEntity {

    private String localDir;
    private String remoteDir;

    public Mapping(String localDir, String remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
    }

    @Override
    public String getLocalDir() {
        return localDir;
    }

    @Override
    public String getRemoteDir() {
        return remoteDir;
    }

    @Override
    public String getId() {
        return localDir + ":" + remoteDir;
    }
}
