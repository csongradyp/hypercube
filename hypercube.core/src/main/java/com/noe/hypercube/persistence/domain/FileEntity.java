package com.noe.hypercube.persistence.domain;

import java.util.Date;

public interface FileEntity extends IEntity<String>, Comparable<FileEntity> {

    String getLocalPath();

    String getRemotePath();

    void setRemotePath(String remotePath);

    String getRevision();

    void setRevision(String revision);

    Date lastModified();

    void setLastModified(Date lastModified);

    String getAccountName();

    FileEntity duplicate();

    void setLocalPath(String localPath);
}
