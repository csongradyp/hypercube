package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.TestMapping;

public class TestMapper extends DirectoryMapper<TestMapping> {
    @Override
    public Class<TestMapping> getMappingClass() {
        return TestMapping.class;
    }
}
