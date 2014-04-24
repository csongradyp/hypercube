package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.observer.LocalFileListener;
import com.noe.hypercube.service.Dropbox;

import java.util.Collection;

public class DbxLocalFilePreSynchronizer extends LocalFilePreSynchronizer<Dropbox> {

    private Dao<String, DbxFileEntity> dao;

    public DbxLocalFilePreSynchronizer(Dao<String, DbxFileEntity> dao, LocalFileListener<Dropbox> observer) {
        super(observer);
        this.dao = dao;
    }

    @Override
    protected Collection<? extends FileEntity> getMappedFiles() {
        return dao.getAll();
    }

}
