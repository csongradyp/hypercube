package com.noe.hypercube.domain;

import java.nio.file.Path;
import java.util.Date;

public interface ServerEntry {

    String getRevision();

    Path getPath();

    boolean isFolder();

    boolean isFile();

    Date lastModified();
}
