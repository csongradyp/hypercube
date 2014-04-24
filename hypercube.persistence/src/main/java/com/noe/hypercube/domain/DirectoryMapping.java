package com.noe.hypercube.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@MappedSuperclass
@Table(uniqueConstraints= @UniqueConstraint(columnNames = {"localDir", "remoteDir"}))
public abstract class DirectoryMapping implements MappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String localDir;
    private String remoteDir;
    private Filter fileFilters;

    protected DirectoryMapping() {
    }

    public DirectoryMapping(String localDir, String remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        fileFilters = new FileFilter();
    }

    public DirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        this.fileFilters = fileFilters;
    }


    @Override
    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    @Override
    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @Override
    public Filter getFilter() {
        return fileFilters;
    }

    public void setFileFilters(Filter fileFilters) {
        this.fileFilters = fileFilters;
    }

    @Override
    public String getId() {
        return localDir + remoteDir;
    }

    @Override
    public String toString() {
        return "DirectoryMapping{" +
                "localDir=" + localDir +
                ", remoteDir=" + remoteDir +
                ", fileFilters=" + fileFilters +
                '}';
    }
}
