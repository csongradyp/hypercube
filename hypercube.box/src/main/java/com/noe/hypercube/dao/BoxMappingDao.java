package com.noe.hypercube.dao;

import com.noe.hypercube.domain.BoxMapping;
import org.springframework.stereotype.Repository;

@Repository
public class BoxMappingDao extends EntityDao<BoxMapping> {

    @Override
    public Class<BoxMapping> getEntityClass() {
        return BoxMapping.class;
    }
}
