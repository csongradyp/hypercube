package com.noe.hypercube.domain;

import java.nio.file.Path;
import java.util.Date;

public interface ServerEntry {

    /**
     * Returns the remote file id if there is any, otherwise it returns the remote file's revision number. {@link this.getRevision}
     * @return main identifier of the file.
     */
    String getId();

    String getRevision();

    Path getPath();

    boolean isFolder();

    boolean isFile();

    Date lastModified();
}
