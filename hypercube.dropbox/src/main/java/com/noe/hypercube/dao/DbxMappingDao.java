package com.noe.hypercube.dao;

import com.noe.hypercube.domain.DbxMapping;
import org.springframework.stereotype.Repository;

@Repository
public class DbxMappingDao extends EntityDao<DbxMapping> {

    @Override
    public Class<DbxMapping> getEntityClass() {
        return DbxMapping.class;
    }
}
