package com.noe.hypercube.service;


import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.DbxServerEntry;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

public class DbxClientWrapper implements IClient<Dropbox, DbxFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(DbxClientWrapper.class);
    public static final String DROPBOX_FILE_SEPARATOR = "/";

    private final DbxClient client;
    private String cursor;

    public DbxClientWrapper(final DbxClient client) {
        this.client = client;
        cursor = null;
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
    public boolean exist(UploadEntity uploadEntity) {
        return exist(uploadEntity.getRemoteFilePath().toString());
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
        LOG.debug("{} exists = {}", dropboxFilePath, exists);
        return exists;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();
        try {
            DbxDelta<DbxEntry> delta = client.getDelta(cursor);
            for (DbxDelta.Entry<DbxEntry> entry : delta.entries) {
                if (entry.metadata.isFile()) {
                    final ServerEntry dbxServerEntry = new DbxServerEntry(entry.lcPath, entry.metadata.asFile().rev, entry.metadata.asFile().lastModified, entry.metadata.isFolder());
                    serverEntries.add(dbxServerEntry);
                }
            }
            cursor = delta.cursor;
        } catch (Exception e) {
            throw new SynchronizationException("Could not get changes from Dropbox", e);
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
            throw new SynchronizationException(String.format("Failed to write to disc: %s", serverEntry));
        }
    }

    @Override
    public void download(String serverEntry, FileOutputStream outputStream, Object... extraArgs) {
    }

    @Override
    public void delete(File fileToUpload, Path remoteFolder) throws SynchronizationException {
        final String dropboxFilePath = getDropboxPath(remoteFolder) + DROPBOX_FILE_SEPARATOR + fileToUpload.getName();
        try {
            client.delete(dropboxFilePath);
        } catch (DbxException e) {
            LOG.error("Dropbox file deletion failed from server: {}", remoteFolder);
            throw new SynchronizationException("Dropbox file deletion failed from server: " + remoteFolder);
        }
    }

    @Override
    public ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException {
        return upload(uploadEntity, DbxWriteMode.add());
    }

    @Override
    public ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        return upload(uploadEntity, DbxWriteMode.force());
    }

    private ServerEntry upload(UploadEntity uploadEntity, DbxWriteMode writeMode) throws SynchronizationException {
        final Path remoteFilePath = uploadEntity.getRemoteFilePath();
        final String dropboxFilePath = getDropboxPath(remoteFilePath);
        final File fileToUpload = uploadEntity.getFile();
        DbxServerEntry serverEntry = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(fileToUpload)) {
            DbxEntry.File uploadedFile = client.uploadFile(dropboxFilePath, writeMode, fileToUpload.length(), inputStream);
            serverEntry = new DbxServerEntry(uploadedFile.path, uploadedFile.rev, uploadedFile.lastModified, uploadedFile.isFolder());
        } catch (DbxException e) {
            LOG.error("File upload failed to Dropbox: {}", remoteFilePath);
        } catch (IOException e) {
            LOG.error("Could not read file to upload: {}", fileToUpload.getPath());
            throw new SynchronizationException(String.format("Upload failed - Cannot read file: %s", fileToUpload.toPath().toString()), e);
        }
        return serverEntry;
    }

    private String getDropboxPath(Path remotePath) {
        return remotePath.toString().replace("\\", DROPBOX_FILE_SEPARATOR);
    }
}
