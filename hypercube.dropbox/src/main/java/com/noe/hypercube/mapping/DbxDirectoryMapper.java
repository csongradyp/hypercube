package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.DbxMapping;
import com.noe.hypercube.service.Dropbox;

public class DbxDirectoryMapper extends DirectoryMapper<Dropbox, DbxMapping> {

    @Override
    public Class<DbxMapping> getMappingClass() {
        return DbxMapping.class;
    }

    @Override
    public Class<Dropbox> getAccountType() {
        return Dropbox.class;
    }

}
