package com.noe.hypercube.domain;

import com.noe.hypercube.service.AccountType;

public interface MappingEntity extends IEntity<String> {

    String getRemoteDir();

    String getLocalDir();

    Filter getFilter();

    Class<? extends AccountType> getAccountType();
}
