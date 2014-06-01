package com.noe.hypercube.googledrive.service;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.googledrive.domain.DriveFileEntity;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;

public class DriveClient implements IClient<GoogleDrive, DriveFileEntity> {

    //TODO implement!!!

    @Override
    public String getAccountName() {
        return null;
    }

    @Override
    public Class<DriveFileEntity> getEntityType() {
        return DriveFileEntity.class;
    }

    @Override
    public Class<GoogleDrive> getAccountType() {
        return GoogleDrive.class;
    }

    @Override
    public boolean exist(Path remotePath) {
        return false;
    }

    @Override
    public boolean exist(ServerEntry serverEntry) {
        return false;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        return null;
    }

    @Override
    public void download(ServerEntry serverPath, OutputStream outputStream) throws SynchronizationException {
      
    }

    @Override
    public void download(String serverPath, OutputStream outputStream, Object... extraArgs) throws SynchronizationException {
      
    }

    @Override
    public void delete(Path remotePath) throws SynchronizationException {
      
    }

    @Override
    public ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return null;
    }

    @Override
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return null;
    }
}
