package com.noe.hypercube.domain;

import java.nio.file.Path;
import java.util.Date;

public interface ServerEntry {

    String getRevision();

    Path getPath();

    Long getSize();

    boolean isFolder();

    boolean isFile();

    boolean isShared();

    Date lastModified();

    /**
     * Returns the remote file id if there is any, otherwise it returns the remote file's revision number. {@link this.getRevision}
     * @return main identifier of the file.
     */
    String getId();

    String getAccount();
}
