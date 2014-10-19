package com.noe.hypercube.domain;


import java.nio.file.Path;
import java.util.Date;

public class DefaultFileServerEntry implements ServerEntry {

    private final String account;
    private String id;
    private Path remoteFile;
    private String revision;
    private Date lastModified;

    public DefaultFileServerEntry(String account, Path remoteFile) {
        this.account = account;
        this.remoteFile = remoteFile;
    }

    public DefaultFileServerEntry(String account, String id) {
        this.account = account;
        this.id = id;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public Path getPath() {
        return remoteFile;
    }

    @Override
    public Long getSize() {
        return null;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isShared() {
        return false;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Date lastModified() {
        return lastModified;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "Dropbox File [ "
                + remoteFile
                + ", rev: " + revision
                + ", lastModDate: " + lastModified
                + " ]";
    }
}
