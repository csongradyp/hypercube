package com.noe.hypercube.googledrive.domain;


import com.noe.hypercube.domain.DirectoryMapping;
import com.noe.hypercube.domain.Filter;

import javax.persistence.Entity;
import java.nio.file.Path;

@Entity
public class DriveDirectoryMapping extends DirectoryMapping {

    public DriveDirectoryMapping() {
    }

    public DriveDirectoryMapping(Path localDir, Path remoteDir) {
        super(localDir, remoteDir);
    }

    public DriveDirectoryMapping(Path localDir, Path remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }

    public DriveDirectoryMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    public DriveDirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }
}
