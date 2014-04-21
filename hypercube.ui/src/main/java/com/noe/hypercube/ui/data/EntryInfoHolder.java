package com.noe.hypercube.ui.data;


import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.synchronization.Action;

import java.util.Date;

public class EntryInfoHolder implements FileEntity {

    private Action action;
    private String localPath;
    private String revision;
    private Date lastModifiedDate;

    public EntryInfoHolder(FileEntity entry, Action action) {
        this.action = action;
        this.localPath = entry.getLocalPath();
        this.revision = entry.getRevision();
        this.lastModifiedDate = entry.lastModified();
    }

    @Override
    public String getLocalPath() {
       return localPath;
    }

    @Override
    public void setLocalPath(String s) {
    }

    @Override
    public String getRevision() {
       return revision;
    }

    @Override
    public void setRevision(String s) {
    }

    @Override
    public Date lastModified() {
       return lastModifiedDate;
    }

    @Override
    public void setLastModified(Date date) {
    }

    @Override
    public String getId() {
        return localPath;
    }

    @Override
    public int compareTo(FileEntity iDbEntry) {
        if(localPath.equals(iDbEntry.getLocalPath())) {
            return 0;
        }
        return -1;
    }

    public Action getAction() {
        return action;
    }
}
