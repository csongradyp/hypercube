package com.noe.hypercube.googledrive.dao;


import com.noe.hypercube.dao.EntityDao;
import com.noe.hypercube.googledrive.domain.DriveMapping;
import org.springframework.stereotype.Repository;

@Repository
public class DriveMappingDao extends EntityDao<DriveMapping> {

    @Override
    public Class<DriveMapping> getEntityClass() {
        return DriveMapping.class;
    }
}
