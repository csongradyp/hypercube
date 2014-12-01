package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.service.TestAccount;

public class TestMapping implements MappingEntity {

    private String localDir;
    private String remoteDir;
    private Filter filters;

    public TestMapping(String localdir, String remoteDir) {
        this.localDir = localdir;
        this.remoteDir = remoteDir;
        filters = new TestFilter();
    }

    @Override
    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @Override
    public String getRemoteDir() {
        return remoteDir;
    }

    @Override
    public void setLocalDir(String localDir) {
        this.localDir = localDir;
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
    public String getAccountName() {
        return "test";
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
