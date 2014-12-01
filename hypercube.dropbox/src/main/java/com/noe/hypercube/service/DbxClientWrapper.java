package com.noe.hypercube.service;


import com.dropbox.core.*;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbxClientWrapper extends Client<Dropbox, DbxClient, DbxFileEntity, DbxMapping> {

    private static final Logger LOG = LoggerFactory.getLogger(DbxClientWrapper.class);

    private String cursor;

    @Inject
    public DbxClientWrapper(final Authentication<DbxClient> dbxAuthentication) {
        super(dbxAuthentication);
    }

    @Override
    protected boolean testConnectionActive() {
        try {
            return getClient().getAccountInfo() != null;
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
        return exist(getDropboxPath(uploadEntity.getRemoteFilePath().toString()));
    }

    @Override
    public boolean exist(final ServerEntry serverEntry) {
        return exist(getDropboxPath(serverEntry.getPath()));
    }

    private boolean exist(final String dropboxFilePath) {
        boolean exists = false;
        try {
            final DbxEntry.WithChildren metadata = getClient().getMetadataWithChildren(dropboxFilePath);
            exists = metadata != null;
        } catch (DbxException e) {
            LOG.error("Failed to get file information from Dropbox: {}", dropboxFilePath, e);
        }
        return exists;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> serverEntries = new LinkedList<>();
        try {
            DbxDelta<DbxEntry> delta = getClient().getDelta(cursor);
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
            getClient().getFile(dropboxPath, serverEntry.getRevision(), outputStream);
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
            final DbxEntry.File file = getClient().getFile(serverPath, null, outputStream);
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
            getClient().delete(dropboxFilePath);
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
            DbxEntry.File uploadedFile = getClient().uploadFile(dropboxFilePath, writeMode, fileToUpload.length(), inputStream);
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
    public FileEntity rename(FileEntity remoteFile, String newName) throws SynchronizationException {
        final Path remoteFolder = Paths.get(remoteFile.getRemotePath()).getParent();
        try {
            final DbxEntry renamedFile = getClient().move(getDropboxPath(remoteFile.getRemotePath()), getDropboxPath(remoteFolder + newName));
            return new DbxFileEntity(remoteFile.getLocalPath(), renamedFile.path, renamedFile.asFile().rev);
        } catch (DbxException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getRemotePath()), e);
        }
    }

    @Override
    public FileEntity rename(ServerEntry remoteFile, String newName) throws SynchronizationException {
        final Path remoteFolder = remoteFile.getPath().getParent();
        try {
            final DbxEntry renamedFile = getClient().move(getDropboxPath(remoteFile.getPath()), getDropboxPath(remoteFolder + "/" + newName));
            return new DbxFileEntity(null, renamedFile.path, renamedFile.asFile().rev);
        } catch (DbxException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getPath()), e);
        }
    }

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final String dropboxPath = remoteFolder.toString().replace("\\", "/");
        final List<ServerEntry> fileList = new ArrayList<>();
        try {
            final DbxEntry.WithChildren metadataWithChildren = getClient().getMetadataWithChildren(dropboxPath);
            final List<DbxEntry> folderContent = metadataWithChildren.children;
            for (DbxEntry file : folderContent) {
                fileList.add(getDbxFileInfo(file));
            }
        } catch (DbxException e) {
            throw new SynchronizationException(e.getMessage(), e);
        }
        return fileList;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> fileList = new ArrayList<>();
        try {
            final DbxEntry.WithChildren metadataWithChildren = getClient().getMetadataWithChildren("/");
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
            final DbxEntry.Folder createdFolder = getClient().createFolder(folderPath);
        } catch (DbxException e) {
            throw new SynchronizationException("Unable to create folder: " + folder);
        }
    }

    private String getDropboxPath(final Path remotePath) {
        String dropboxPath = remotePath.toString();
        return getDropboxPath(dropboxPath);
    }

    private String getDropboxPath(String path) {
        String dropboxPath = path.replace("\\", "/");
        if (!dropboxPath.startsWith("/")) {
            dropboxPath = "/" + dropboxPath;
        }
        return dropboxPath;
    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        try {
            final DbxAccountInfo.Quota quota = getClient().getAccountInfo().quota;
            return new AccountQuota(quota.total, quota.normal + quota.shared);
        } catch (DbxException e) {
            throw new SynchronizationException("Could not get Dropbox qouta info", e);
        }
    }
}
