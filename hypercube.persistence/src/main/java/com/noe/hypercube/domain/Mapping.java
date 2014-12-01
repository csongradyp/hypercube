package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.MappingEntity;
import javax.persistence.*;

@MappedSuperclass
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"localDir", "remoteDir"}))
public abstract class Mapping implements MappingEntity {

    @Id
    private String id;

    private String localDir;
    private String remoteDir;

    protected Mapping() {
    }

    public Mapping(final String localDir, final String remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        id = localDir + remoteDir;
    }

    @Override
    public String getLocalDir() {
        return localDir;
    }

    @Override
    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    @Override
    public String getRemoteDir() {
        return remoteDir;
    }

    @Override
    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @Override
    public Filter getFilter() {
//        return fileFilters;
        // TODO fix persistence to accept FileFilter
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "localDir=" + localDir +
                ", remoteDir=" + remoteDir +
                '}';
    }
}
