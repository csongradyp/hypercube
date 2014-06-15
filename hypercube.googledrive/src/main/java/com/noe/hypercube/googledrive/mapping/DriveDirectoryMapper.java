package com.noe.hypercube.googledrive.mapping;

import com.noe.hypercube.googledrive.domain.DriveMapping;
import com.noe.hypercube.googledrive.service.GoogleDrive;
import com.noe.hypercube.mapping.DirectoryMapper;

public class DriveDirectoryMapper extends DirectoryMapper<GoogleDrive, DriveMapping> {

    @Override
    public Class<DriveMapping> getMappingClass() {
        return DriveMapping.class;
    }

    @Override
    public Class<GoogleDrive> getAccountType() {
        return GoogleDrive.class;
    }

}
