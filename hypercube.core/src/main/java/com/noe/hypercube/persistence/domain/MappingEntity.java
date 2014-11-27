package com.noe.hypercube.persistence.domain;

import com.noe.hypercube.domain.Filter;
import com.noe.hypercube.service.Account;

public interface MappingEntity extends IEntity<String> {

    void setRemoteDir(String remoteDir);

    String getRemoteDir();

    void setLocalDir(String localDir);

    String getLocalDir();

    Filter getFilter();

    Class<? extends Account> getAccountType();

    String getAccountName();
}
