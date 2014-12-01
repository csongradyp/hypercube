package com.noe.hypercube.dao;

import com.noe.hypercube.domain.DriveMapping;

public class DriveMappingDao extends Dao<DriveMapping> {

    @Override
    public Class<DriveMapping> getEntityClass() {
        return DriveMapping.class;
    }
}
