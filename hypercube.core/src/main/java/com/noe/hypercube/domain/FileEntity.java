package com.noe.hypercube.domain;

import java.util.Date;

public interface FileEntity extends IEntity<String>, Comparable<FileEntity> {

    String getLocalPath();

    String getRemotePath();

    String getRevision();

    void setRevision(String revision);

    Date lastModified();

    void setLastModified(Date lastModified);

    String getAccountName();
}
