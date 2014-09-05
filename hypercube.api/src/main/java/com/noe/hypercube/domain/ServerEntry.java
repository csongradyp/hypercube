package com.noe.hypercube.domain;

import java.nio.file.Path;
import java.util.Date;

public interface ServerEntry {

    String getRevision();

    Path getPath();

    Long getSize();

    boolean isFolder();

    boolean isFile();

    Date lastModified();

    String getId();

    String getAccount();
}
