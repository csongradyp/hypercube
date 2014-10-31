package com.noe.hypercube.googledrive.dao;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.googledrive.domain.DriveFileEntity;

public class DriveFileEntryDao extends Dao<DriveFileEntity> {

    @Override
    public Class<DriveFileEntity> getEntityClass() {
        return DriveFileEntity.class;
    }
}
