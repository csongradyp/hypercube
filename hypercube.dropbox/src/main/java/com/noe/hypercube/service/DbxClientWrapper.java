package com.noe.hypercube.service;


import com.dropbox.core.*;
import com.noe.hypercube.domain.AccountQuota;
import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.DbxServerEntry;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.domain.ServerEntryEvent;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class DbxClientWrapper implements IClient<Dropbox, DbxFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(DbxClientWrapper.class);

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
    public boolean exist(File fileToUpload, Path remotePath) {
        return exist(getDropboxPath(remotePath) + "/" + fileToUpload.getName());
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

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final String dropboxPath = remoteFolder.toString().replace("\\", "/");
        final List<ServerEntry> fileList = new ArrayList<>();
        try {
            final DbxEntry.WithChildren metadataWithChildren = client.getMetadataWithChildren(dropboxPath);
            final List<DbxEntry> folderContent = metadataWithChildren.children;
            for (DbxEntry file : folderContent) {
                String rev = "";
                Date lastModified = new Date();
                if (file.isFile()) {
                    lastModified = file.asFile().lastModified;
                    rev = file.asFile().rev;
                }
                fileList.add(new ServerEntryEvent(file.path, rev, lastModified, file.isFolder()));
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
                String rev = "";
                Date lastModified = new Date();
                if (file.isFile()) {
                    lastModified = file.asFile().lastModified;
                    rev = file.asFile().rev;
                }
                fileList.add(new ServerEntryEvent(file.path, rev, lastModified, file.isFolder()));
            }
        } catch (DbxException e) {
            throw new SynchronizationException(e.getMessage());
        }
        return fileList;
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

    private ServerEntry upload(final Path remotePath, final DbxWriteMode writeMode, final File fileToUpload, final InputStream inputStream) {
        DbxServerEntry serverEntry = null;
        final String dropboxFilePath = getDropboxPath(remotePath) + "/" + fileToUpload.getName();
        try {
            DbxEntry.File uploadedFile = client.uploadFile(dropboxFilePath, writeMode, fileToUpload.length(), inputStream);
            serverEntry = new DbxServerEntry(uploadedFile.path, uploadedFile.rev, uploadedFile.lastModified, uploadedFile.isFolder());
        } catch (DbxException e) {
            LOG.error("File upload failed to Dropbox: {}", remotePath);
        } catch (IOException e) {
            LOG.error("Could not read file to upload: {}", fileToUpload.getPath());
        }
        return serverEntry;
    }

    private String getDropboxPath(final Path remotePath) {
        String dropboxPath = remotePath.toString();
        if(!dropboxPath.startsWith("/")) {
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
