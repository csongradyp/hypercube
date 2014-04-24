package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.TestMapping;
import com.noe.hypercube.service.TestAccount;

public class TestMapper extends DirectoryMapper<TestMapping, TestAccount> {

    @Override
    public Class<TestMapping> getMappingClass() {
        return TestMapping.class;
    }

    @Override
    public Class<TestAccount> getAccountType() {
        return TestAccount.class;
    }
}
