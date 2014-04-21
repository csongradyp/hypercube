package com.noe.hypercube.dao;


import com.noe.hypercube.domain.DbxFileEntity;

public class DbxEntityDao extends EntityDao<DbxFileEntity> {

    @Override
    public Class<DbxFileEntity> getEntityClass() {
        return DbxFileEntity.class;
    }

}
