package com.noe.hypercube.dao;

import com.noe.hypercube.domain.DbxMapping;

public class DbxMappingDao extends Dao<DbxMapping> {

    @Override
    public Class<DbxMapping> getEntityClass() {
        return DbxMapping.class;
    }
}
