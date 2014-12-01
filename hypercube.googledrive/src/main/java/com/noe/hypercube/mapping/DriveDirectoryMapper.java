package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.DriveMapping;
import com.noe.hypercube.service.GoogleDrive;

public class DriveDirectoryMapper extends DirectoryMapper<GoogleDrive, DriveMapping> {

    @Override
    public Class<DriveMapping> getMappingClass() {
        return DriveMapping.class;
    }

    @Override
    public Class<GoogleDrive> getAccountType() {
        return GoogleDrive.class;
    }

    @Override
    public DriveMapping createMapping() {
        return new DriveMapping();
    }

    @Override
    protected String getAccountName() {
        return GoogleDrive.name;
    }
}
