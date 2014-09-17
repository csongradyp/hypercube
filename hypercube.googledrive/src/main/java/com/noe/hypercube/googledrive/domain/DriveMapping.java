package com.noe.hypercube.googledrive.domain;

import com.noe.hypercube.domain.Mapping;
import com.noe.hypercube.googledrive.service.GoogleDrive;
import com.noe.hypercube.service.Account;

import javax.persistence.Entity;

@Entity
public class DriveMapping extends Mapping {

    public DriveMapping() {
    }

    public DriveMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    @Override
    public Class<? extends Account> getAccountType() {
        return GoogleDrive.class;
    }
}
