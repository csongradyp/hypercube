package com.noe.hypercube.event.dto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class RemoteFileInfo {

    private final Path path;
    private final String accountName;
    private final Boolean folder;
    private String id;
    private Long size;
    private Date lastModified;

    public RemoteFileInfo(final String accountName, final Path path, final Boolean folder) {
        this.accountName = accountName;
        this.path = path;
        this.folder = folder;
    }

    public RemoteFileInfo(final String accountName, String path, final Boolean folder) {
        this.accountName = accountName;
        this.path = Paths.get(path);
        this.folder = folder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date lastModified() {
        return lastModified;
    }

    public Path getPath() {
        return path;
    }

    public String getAccountName() {
        return accountName;
    }

    public Boolean isFolder() {
        return folder;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getSize() {
        return size;
    }
}
