package com.noe.hypercube.domain;

import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.Dropbox;

import javax.persistence.Entity;

@Entity
public class DbxMapping extends Mapping {

    public DbxMapping() {
    }

    public DbxMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

//    public DbxMapping(String localDir, String remoteDir, FileFilter fileFilters) {
//        super(localDir, remoteDir, fileFilters);
//    }

    @Override
    public Class<? extends Account> getAccountType() {
        return Dropbox.class;
    }
}
