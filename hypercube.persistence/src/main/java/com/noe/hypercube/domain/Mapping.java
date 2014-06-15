package com.noe.hypercube.domain;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@MappedSuperclass
@Table(uniqueConstraints= @UniqueConstraint(columnNames = {"localDir", "remoteDir"}))
public abstract class Mapping implements MappingEntity {

    @Id
    private String id;

    private String localDir;
    private String remoteDir;
//    @OneToOne(fetch= FetchType.EAGER)
//    private FileFilter fileFilters;

    protected Mapping() {
    }

    public Mapping(String localDir, String remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
//        fileFilters = new FileFilter();
    }

//    public Mapping(String localDir, String remoteDir, FileFilter fileFilters) {
//        this.localDir = localDir;
//        this.remoteDir = remoteDir;
//        this.fileFilters = fileFilters;
//    }


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
//        return fileFilters;
        // TODO fix persistence to accept FileFilter
        return null;
    }

//    public void setFileFilters(FileFilter fileFilters) {
//        this.fileFilters = fileFilters;
//    }

    @Override
    public String getId() {
        return localDir + remoteDir;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "localDir=" + localDir +
                ", remoteDir=" + remoteDir +
//                ", fileFilters=" + fileFilters +
                '}';
    }
}
