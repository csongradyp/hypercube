package com.noe.hypercube.mapping;


import com.noe.hypercube.domain.DbxDirectoryMapping;
import com.noe.hypercube.service.Dropbox;

public class DbxDirectoryMapper extends DirectoryMapper<Dropbox, DbxDirectoryMapping> {

    @Override
    public Class<DbxDirectoryMapping> getMappingClass() {
        return DbxDirectoryMapping.class;
    }

    @Override
    public Class<Dropbox> getAccountType() {
        return Dropbox.class;
    }

}
