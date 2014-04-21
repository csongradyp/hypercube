package com.noe.hypercube.mapping;


import com.noe.hypercube.domain.DbxDirectoryMapping;
import com.noe.hypercube.domain.DbxFileEntity;

public class DbxDirectoryMapper extends DirectoryMapper<DbxDirectoryMapping, DbxFileEntity> {

    @Override
    public Class<DbxDirectoryMapping> getMappingClass() {
        return DbxDirectoryMapping.class;
    }

    @Override
    public Class<DbxFileEntity> getEntityClass() {
        return DbxFileEntity.class;
    }


}
