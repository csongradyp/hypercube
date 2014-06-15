package com.noe.hypercube.googledrive.domain;

import com.noe.hypercube.domain.Mapping;
import com.noe.hypercube.googledrive.service.GoogleDrive;
import com.noe.hypercube.service.Account;


public class DriveMapping extends Mapping {

    @Override
    public Class<? extends Account> getAccountType() {
        return GoogleDrive.class;
    }
}
