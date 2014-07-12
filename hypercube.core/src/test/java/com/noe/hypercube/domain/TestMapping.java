package com.noe.hypercube.domain;

import com.noe.hypercube.service.TestAccount;

public class TestMapping implements MappingEntity {

    private final String localDir;
    private final String remoteDir;
    private final Filter filters;

    public TestMapping(String localdir, String remoteDir) {
        this.localDir = localdir;
        this.remoteDir = remoteDir;
        filters = new TestFilter();
    }

    @Override
    public String getRemoteDir() {
        return remoteDir;
    }

    @Override
    public String getLocalDir() {
        return localDir;
    }

    @Override
    public Filter getFilter() {
        return filters;
    }

    @Override
    public Class<TestAccount> getAccountType() {
        return TestAccount.class;
    }

    @Override
    public String getId() {
        return localDir;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "localDir=" + localDir +
                ", remoteDir=" + remoteDir +
                ", fileFilters=" + filters +
                '}';
    }
}
