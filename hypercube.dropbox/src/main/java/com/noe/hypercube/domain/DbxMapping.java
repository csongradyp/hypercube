package com.noe.hypercube.domain;

import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.Dropbox;

import javax.persistence.Entity;

@Entity
public class DbxMapping extends Mapping {

    public DbxMapping() {
    }

    public DbxMapping(final String localDir, final String remoteDir) {
        super(localDir, remoteDir);
    }

    @Override
    public Class<? extends Account> getAccountType() {
        return Dropbox.class;
    }
}
