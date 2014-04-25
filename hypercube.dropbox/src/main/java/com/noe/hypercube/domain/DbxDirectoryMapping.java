package com.noe.hypercube.domain;

import com.noe.hypercube.service.AccountType;
import com.noe.hypercube.service.Dropbox;

import javax.persistence.Entity;

@Entity
public class DbxDirectoryMapping extends DirectoryMapping {

    public DbxDirectoryMapping() {
    }

    public DbxDirectoryMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    public DbxDirectoryMapping(String localDir, String remoteDir, Filter fileFilters) {
        super(localDir, remoteDir, fileFilters);
    }

    @Override
    public Class<? extends AccountType> getAccountType() {
        return Dropbox.class;
    }
}
