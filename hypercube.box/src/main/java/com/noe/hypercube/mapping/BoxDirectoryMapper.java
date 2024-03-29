package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.BoxMapping;
import com.noe.hypercube.service.Box;

public class BoxDirectoryMapper extends DirectoryMapper<Box, BoxMapping> {

    @Override
    public Class<BoxMapping> getMappingClass() {
        return BoxMapping.class;
    }

    @Override
    public Class<Box> getAccountType() {
        return Box.class;
    }

    @Override
    public BoxMapping createMapping() {
        return new BoxMapping();
    }

    @Override
    protected String getAccountName() {
        return Box.getName();
    }
}
