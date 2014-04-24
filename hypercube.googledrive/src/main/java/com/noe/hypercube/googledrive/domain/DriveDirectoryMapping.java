package com.noe.hypercube.googledrive.domain;

import com.noe.hypercube.domain.DirectoryMapping;
import com.noe.hypercube.domain.Filter;
import com.noe.hypercube.service.AccountType;

import javax.persistence.Entity;

@Entity
public class DriveDirectoryMapping extends DirectoryMapping {

    public DriveDirectoryMapping() {
    }

    public DriveDirectoryMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    public DriveDirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }

    @Override
    public Class<? extends AccountType> getAccountType() {
        return null;
    }
}
