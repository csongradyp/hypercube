package com.noe.hypercube.domain;

import com.noe.hypercube.service.Account;

public interface MappingEntity extends IEntity<String> {

    String getRemoteDir();

    String getLocalDir();

    Filter getFilter();

    Class<? extends Account> getAccountType();
}
