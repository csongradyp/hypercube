package com.noe.hypercube.dao;

import com.noe.hypercube.domain.DriveFileEntity;

public class DriveFileEntryDao extends Dao<DriveFileEntity> {

    @Override
    public Class<DriveFileEntity> getEntityClass() {
        return DriveFileEntity.class;
    }
}
