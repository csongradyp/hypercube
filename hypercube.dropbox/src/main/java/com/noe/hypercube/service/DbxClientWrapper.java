package com.noe.hypercube.service;


import com.dropbox.core.*;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DbxClientWrapper extends Client<Dropbox, DbxFileEntity, DbxMapping> {

    private static final Logger LOG = LoggerFactory.getLogger(DbxClientWrapper.class);

    private final DbxClient client;
    private String cursor;

    public DbxClientWrapper(final DbxClient client) {
        this.client = client;
        cursor = null;
    }

    @Override
    protected boolean testConnectionActive() {
        try {
            return client.getAccountInfo() != null;
        } catch (DbxException e) {
            return false;
        }
    }

    @Override
    public String getAccountName() {
        return Dropbox.getName();
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
    public Class<DbxMapping> getMappingType() {
        return DbxMapping.class;
    }

    @Override
    public boolean exist(final UploadEntity uploadEntity) {
        return exist(uploadEntity.getRemoteFilePath().toString());
    }

    @Override
    public boolean exist(final ServerEntry serverEntry) {
        return exist(getDropboxPath(serverEntry.getPath()));
    }

    private boolean exist(final String dropboxFilePath) {
        boolean exists = false;
        try {
            exists = client.getMetadata(dropboxFilePath) != null;
        } catch (DbxException e) {
            LOG.error("Failed to get file information from Dropbox: {}", dropboxFilePath);
        }
        LOG.debug(dropboxFilePath + " exists = " + exists);
        return exists;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();
        try {
            DbxDelta<DbxEntry> delta = client.getDelta(cursor);
            for (DbxDelta.Entry<DbxEntry> entry : delta.entries) {
                if (entry.metadata.isFile()) {
                    DbxServerEntry dbxServerEntry = new DbxServerEntry(entry.lcPath, entry.metadata.asFile().rev, entry.metadata.asFile().lastModified, entry.metadata.isFolder());
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
            throw new SynchronizationException("Failed to write to disc: " + serverEntry);
        }
    }

    @Override
    public ServerEntry download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        try {
            final DbxEntry.File file = client.getFile(serverPath, null, outputStream);
            return new DbxServerEntry(file.path, file.rev, file.lastModified, file.isFolder());
        } catch (DbxException e) {
            LOG.error("Dropbox file download failed: {}", serverPath);
            throw new SynchronizationException("Dropbox file download failed: " + serverPath);
        } catch (IOException e) {
            LOG.error("Failed to write downloaded file to disc: {}", serverPath);
            throw new SynchronizationException("Failed to write to disc: " + serverPath);
        }
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {
        final String dropboxFilePath = getDropboxPath(remoteFilePath);
        try {
            client.delete(dropboxFilePath);
        } catch (DbxException e) {
            LOG.error("Dropbox file deletion failed from server: {}", dropboxFilePath);
            throw new SynchronizationException("Dropbox file deletion failed from server: " + dropboxFilePath);
        }
    }

    @Override
    public void delete(final String remoteFileId) throws SynchronizationException {
        throw new UnsupportedOperationException("Dropbox does not populate file id");
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


    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final String dropboxPath = remoteFolder.toString().replace("\\", "/");
        final List<ServerEntry> fileList = new ArrayList<>();
        try {
            final DbxEntry.WithChildren metadataWithChildren = client.getMetadataWithChildren(dropboxPath);
            final List<DbxEntry> folderContent = metadataWithChildren.children;
            for (DbxEntry file : folderContent) {
                fileList.add(getDbxFileInfo(file));
            }
        } catch (DbxException e) {
            throw new SynchronizationException(e.getMessage());
        }
        return fileList;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();
        try {
            final DbxEntry.WithChildren metadataWithChildren = client.getMetadataWithChildren("/");
            final List<DbxEntry> folderContent = metadataWithChildren.children;
            for (DbxEntry file : folderContent) {
                fileList.add(getDbxFileInfo(file));
            }
        } catch (DbxException e) {
            throw new SynchronizationException(e.getMessage());
        }
        return fileList;
    }

    private DbxServerEntry getDbxFileInfo(final DbxEntry file) {
        final DbxServerEntry dbxServerEntry = new DbxServerEntry(file.path, file.isFolder());
        if (file.isFile()) {
            dbxServerEntry.setSize(file.asFile().numBytes);
            dbxServerEntry.setLastModified(file.asFile().lastModified);
            dbxServerEntry.setRevision(file.asFile().rev);
        }
        return dbxServerEntry;
    }

    @Override
    public void createFolder(final Path folder) throws SynchronizationException {
        try {
            final String folderPath = getDropboxPath(folder);
            final DbxEntry.Folder createdFolder = client.createFolder(folderPath);
        } catch (DbxException e) {
            throw new SynchronizationException("Unable to create folder: " + folder);
        }
    }

    private String getDropboxPath(final Path remotePath) {
        String dropboxPath = remotePath.toString();
        if (!dropboxPath.startsWith("/")) {
            dropboxPath = "/" + dropboxPath;
        }
        return dropboxPath.replace("\\", "/");
    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        try {
            final DbxAccountInfo.Quota quota = client.getAccountInfo().quota;
            return new AccountQuota(quota.total, quota.normal + quota.shared);
        } catch (DbxException e) {
            throw new SynchronizationException("Could not get Dropbox qouta info", e);
        }
    }
}
