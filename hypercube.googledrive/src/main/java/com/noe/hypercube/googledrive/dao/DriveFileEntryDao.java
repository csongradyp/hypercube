package com.noe.hypercube.googledrive.dao;

import com.noe.hypercube.googledrive.domain.DriveFileEntity;
import com.noe.hypercube.dao.EntityDao;

public class DriveFileEntryDao extends EntityDao<DriveFileEntity> {

    @Override
    public Class<DriveFileEntity> getEntityClass() {
        return DriveFileEntity.class;
    }
}
