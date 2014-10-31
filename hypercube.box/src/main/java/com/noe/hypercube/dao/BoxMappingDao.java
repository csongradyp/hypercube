package com.noe.hypercube.dao;

import com.noe.hypercube.domain.BoxMapping;

public class BoxMappingDao extends Dao<BoxMapping> {

    @Override
    public Class<BoxMapping> getEntityClass() {
        return BoxMapping.class;
    }
}
