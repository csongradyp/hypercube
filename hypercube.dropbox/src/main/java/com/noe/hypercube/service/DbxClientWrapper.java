package com.noe.hypercube.service;


import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.DbxServerEntry;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DbxClientWrapper implements IClient<Dropbox, DbxFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(DbxClientWrapper.class);

    private DbxClient client;
    private AtomicReference<String> cursor = new AtomicReference<>();

    public DbxClientWrapper(DbxClient client) {
        this.client = client;
        cursor.set("");
    }

    @Override
    public String getAccountName() {
        return "Dropbox";
    }

    @Override
    public Class<Dropbox> getAccountType() {
        return Dropbox.class;
    }

    @Override
    public Class<DbxFileEntity> getEntityType() {
        return DbxFileEntity.class;
    }

    @Override
    public boolean exist(File fileToUpload, Path remotePath) {
        return exist(getDropboxPath(remotePath) + "/" +fileToUpload.getName());
    }

    @Override
    public boolean exist(ServerEntry serverEntry) {
        return exist(getDropboxPath(serverEntry.getPath()));
    }

    private boolean exist(String dropboxFilePath) {
        boolean exists = false;
        try {
            exists = client.getMetadata(dropboxFilePath) != null;
        } catch (DbxException e) {
            LOG.error("Failed to get file information from Dropbox: {}", dropboxFilePath);
        }
        return exists;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();
        try {
            DbxDelta<DbxEntry> delta = client.getDelta(cursor.get());
            List<DbxDelta.Entry<DbxEntry>> entries = delta.entries;
            for (DbxDelta.Entry<DbxEntry> entry : entries) {
                serverEntries.add(new DbxServerEntry(entry.lcPath, entry.metadata.asFile().rev, entry.metadata.asFile().lastModified, entry.metadata.isFolder()));
            }
        } catch (DbxException e) {
            throw new SynchronizationException("Could not get changes from Dropbox");
        }
        return serverEntries;
    }

    @Override
    public void download(ServerEntry serverEntry, FileOutputStream outputStream) throws SynchronizationException {
        final String dropboxPath = getDropboxPath(serverEntry.getPath());
        try {
            client.getFile(dropboxPath, serverEntry.getRevision(), outputStream);
        } catch (DbxException e) {
            LOG.error("Dropbox file download failed: {}", serverEntry.getPath());
            throw new SynchronizationException("Dropbox file download failed: " + serverEntry);
        } catch (IOException e) {
            LOG.error("Failed to write downloaded file to disc: {}", serverEntry.getPath());
            throw new SynchronizationException("Failed to write to disc: " + serverEntry);
        }
    }

    @Override
    public void download(String serverEntry, FileOutputStream outputStream, Object... extraArgs) {
    }

    @Override
    public void delete(File fileToUpload, Path remotePath) throws SynchronizationException {
        final String dropboxFilePath = getDropboxPath(remotePath) + "/" + fileToUpload.getName();
        try {
            client.delete(dropboxFilePath);
        } catch (DbxException e) {
            LOG.error("Dropbox file deletion failed from server: {}", remotePath);
            throw new SynchronizationException("Dropbox file deletion failed from server: " + remotePath);
        }
    }

    @Override
    public ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return upload(remotePath, DbxWriteMode.add(), fileToUpload, inputStream);
    }

    @Override
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        return upload(remotePath, DbxWriteMode.force(), fileToUpload, inputStream);
    }

    private ServerEntry upload(Path remotePath, DbxWriteMode writeMode, File fileToUpload, InputStream inputStream) {
        DbxServerEntry serverEntry = null;
        final String dropboxFilePath = getDropboxPath(remotePath) + "/" + fileToUpload.getName();
        try {
            DbxEntry.File uploadedFile = client.uploadFile(dropboxFilePath,writeMode, fileToUpload.length(), inputStream);
            serverEntry = new DbxServerEntry(uploadedFile.path, uploadedFile.rev, uploadedFile.lastModified, uploadedFile.isFolder());
        } catch (DbxException e) {
            LOG.error("File upload failed to Dropbox: {}", remotePath);
        } catch (IOException e) {
            LOG.error("Could not read file to upload: {}", fileToUpload.getPath());
        }
        return serverEntry;
    }

    private String getDropboxPath(Path remotePath) {
        return remotePath.toString().replace("\\", "/");
    }
}
