package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.DbxDirectoryMapping;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.observer.LocalFilePreChecker;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DbxLocalFilePreChecker extends LocalFilePreChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DbxLocalFilePreChecker.class);

    private Dao<String, DbxFileEntity> dao;

    public DbxLocalFilePreChecker(Dao<String, DbxFileEntity> dao, IUploader dbxUploader, DirectoryMapper<DbxDirectoryMapping> directoryMapper) {
        super(dbxUploader, directoryMapper);
        this.dao = dao;
    }

    @Override
    protected Collection<? extends FileEntity> getMappedFiles() {
        return dao.getAll();
    }
}
