package com.noe.hypercube.googledrive.dao;


import com.noe.hypercube.dao.EntityDao;
import com.noe.hypercube.googledrive.domain.DriveDirectoryMapping;

public class DriveMappingDao  extends EntityDao<DriveDirectoryMapping> {

    @Override
    public Class<DriveDirectoryMapping> getEntityClass() {
        return DriveDirectoryMapping.class;
    }

}
