package com.noe.hypercube.dao;


import com.noe.hypercube.domain.DbxFileEntity;
import org.springframework.stereotype.Repository;

@Repository
public class DbxEntityDao extends EntityDao<DbxFileEntity> {

    @Override
    public Class<DbxFileEntity> getEntityClass() {
        return DbxFileEntity.class;
    }

}
