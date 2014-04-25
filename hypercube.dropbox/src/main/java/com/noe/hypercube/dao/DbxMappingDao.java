package com.noe.hypercube.dao;

import com.noe.hypercube.domain.DbxDirectoryMapping;
import org.springframework.stereotype.Repository;

@Repository
public class DbxMappingDao extends EntityDao<DbxDirectoryMapping> {

    @Override
    public Class<DbxDirectoryMapping> getEntityClass() {
        return DbxDirectoryMapping.class;
    }
}
