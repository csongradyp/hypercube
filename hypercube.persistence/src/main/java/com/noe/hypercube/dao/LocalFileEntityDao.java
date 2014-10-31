package com.noe.hypercube.dao;

import com.noe.hypercube.domain.LocalFileEntity;

import javax.inject.Named;

@Named
public class LocalFileEntityDao extends Dao<LocalFileEntity> {

    @Override
    public Class<LocalFileEntity> getEntityClass() {
        return LocalFileEntity.class;
    }
}
