package com.noe.hypercube.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.nio.file.Path;
import java.nio.file.Paths;

@MappedSuperclass
@Table(uniqueConstraints= @UniqueConstraint(columnNames = {"localDir", "remoteDir"}))
public abstract class DirectoryMapping implements MappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private Path localDir;
    private Path remoteDir;
    private Filter fileFilters;

    protected DirectoryMapping() {
    }

    public DirectoryMapping(Path localDir, Path remoteDir) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        fileFilters = new FileFilter();
    }

    public DirectoryMapping(Path localDir, Path remoteDir, Filter fileFilters) {
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        this.fileFilters = fileFilters;
    }

    public DirectoryMapping(String localDir, String remoteDir) {
        this.localDir = Paths.get(localDir);
        this.remoteDir = Paths.get(remoteDir);
        fileFilters = new FileFilter();
    }

    public DirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        this.localDir = Paths.get(localDir);
        this.remoteDir = Paths.get(remoteDir);
        this.fileFilters = fileFilters;
    }

    @Override
    public Path getLocalDir() {
        return localDir;
    }

    public void setLocalDir(Path localDir) {
        this.localDir = localDir;
    }

    @Override
    public Path getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(Path remoteDir) {
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
        return localDir.normalize().toString();
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
