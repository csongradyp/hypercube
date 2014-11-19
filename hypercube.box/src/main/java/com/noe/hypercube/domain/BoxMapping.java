package com.noe.hypercube.domain;

import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.Box;

import javax.persistence.Entity;

@Entity
public class BoxMapping extends Mapping {

    public BoxMapping() {
    }

    public BoxMapping(String localDir, String remoteDir) {
        super(localDir, remoteDir);
    }

    @Override
    public Class<? extends Account> getAccountType() {
        return Box.class;
    }

    @Override
    public String getAccountName() {
        return Box.getName();
    }
}
