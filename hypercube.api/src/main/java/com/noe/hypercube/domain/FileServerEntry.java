package com.noe.hypercube.domain;


import java.nio.file.Path;
import java.util.Date;

public class FileServerEntry implements ServerEntry {

    private final String account;
    private String id;
    private Path remoteFile;
    private String revision;
    private Date lastModified;

    public FileServerEntry(final String account, final Path remoteFile) {
        this.account = account;
        this.remoteFile = remoteFile;
    }

    public FileServerEntry(final String account, final String id) {
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

    public void setLastModified(final Date lastModified) {
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
        return String.format("FileServerEntry{ account='%s', id='%s', remoteFile=%s, revision='%s', lastModified=%s }", account, id, remoteFile, revision, lastModified);
    }
}
