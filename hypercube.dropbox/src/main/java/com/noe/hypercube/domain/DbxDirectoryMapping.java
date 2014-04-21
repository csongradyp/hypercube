package com.noe.hypercube.domain;

import javax.persistence.Entity;
import java.nio.file.Path;

@Entity
public class DbxDirectoryMapping extends DirectoryMapping {

    public DbxDirectoryMapping() {
    }

    public DbxDirectoryMapping(Path localDir, Path remoteDir) {
        super(localDir, remoteDir);
    }

    public DbxDirectoryMapping(Path localDir, Path remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }

    public DbxDirectoryMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    public DbxDirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }
}
