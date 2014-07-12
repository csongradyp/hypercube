package com.noe.hypercube.domain;

import java.util.Date;

public interface FileEntity extends IEntity<String>, Comparable<FileEntity> {

    String getLocalPath();

    void setLocalPath(String localPath);

    String getRevision();

    void setRevision(String revision);

    Date lastModified();

    void setLastModified(Date lastModified);

}
