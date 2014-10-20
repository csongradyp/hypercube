package com.noe.hypercube.googledrive.dao;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.googledrive.domain.DriveMapping;

public class DriveMappingDao extends Dao<DriveMapping> {

    @Override
    public Class<DriveMapping> getEntityClass() {
        return DriveMapping.class;
    }
}
