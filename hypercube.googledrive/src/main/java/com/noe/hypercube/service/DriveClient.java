package com.noe.hypercube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountQuota;
import com.noe.hypercube.domain.DriveServerEntry;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.domain.DriveFileEntity;
import com.noe.hypercube.domain.DriveMapping;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriveClient extends Client<GoogleDrive, Drive, DriveFileEntity, DriveMapping> {

    private static final Logger LOG = LoggerFactory.getLogger(DriveClient.class);
    private static final String EXCLUDE_FILE = "collaboration";

    @Inject
    private IPersistenceController persistenceController;
    private DriveDirectoryUtil dirUtil;

    private Long cursor;

    public DriveClient(final Authentication<Drive> driveAuthentication) {
        super(driveAuthentication);
    }

    @PostConstruct
    public void init() {
        dirUtil = new DriveDirectoryUtil(getClient());
    }

    @Override
    public boolean testConnection() {
        try {
            final FileList list = getClient().files().list().execute();
            return list != null;
        } catch (GoogleJsonResponseException e) {
            LOG.error("Server error while testing connection - error: ", e);
            return false;
        } catch (IOException e) {
            LOG.error("Error while testing connection - error: ", e);
            return false;
        }
    }

    @Override
    public String getAccountName() {
        return GoogleDrive.name;
    }

    @Override
    public Class<DriveFileEntity> getEntityType() {
        return DriveFileEntity.class;
    }

    @Override
    public Class<DriveMapping> getMappingType() {
        return DriveMapping.class;
    }

    @Override
    public Class<GoogleDrive> getAccountType() {
        return GoogleDrive.class;
    }

    @Override
    public boolean exist(final UploadEntity uploadEntity) {
        DriveFileEntity fileEntity = (DriveFileEntity) persistenceController.get(uploadEntity.getFile().toPath().toString(), DriveFileEntity.class);
        return isExisting(fileEntity.getFileId());
    }

    @Override
    public boolean exist(final ServerEntry serverEntry) {
        DriveServerEntry driveServerEntry = (DriveServerEntry) serverEntry;
        com.google.api.services.drive.model.File remoteFile = driveServerEntry.getRemoteFile();
        return isExisting(remoteFile.getId());
    }

    private boolean isExisting(String fileId) {
        com.google.api.services.drive.model.File file = null;
        try {
            file = getClient().files().get(fileId).execute();
        } catch (IOException e) {

        }
        return file != null;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        Collection<ServerEntry> changes = new ArrayList<>();
        try {
            final List<Change> changeItems = retrieveAllChanges(cursor);
            for (Change change : changeItems) {
                final com.google.api.services.drive.model.File file = change.getFile();
                final String path = dirUtil.getPath(file);
                final String revision = file.getHeadRevisionId();
                final Date lastModified = new Date(change.getModificationDate().getValue());
                changes.add(new DriveServerEntry(file, path, revision, lastModified, dirUtil.isFolder(file)));
                cursor = change.getId();
            }
            return getFileList();
        } catch (IOException e) {
            throw new SynchronizationException("An error while getting file list from Google Drive Server ", e);
        }
    }

    /**
     * @param startChangeId Id of the last synchronize {@link com.google.api.services.drive.model.Change}
     * @return List of {@link com.google.api.services.drive.model.Change}s
     * @throws SynchronizationException in case of any errors
     */
    private List<Change> retrieveAllChanges(final Long startChangeId) throws SynchronizationException {
        try {
            Drive.Changes.List request = getClient().changes().list();
            if (startChangeId != null) {
                request.setStartChangeId(startChangeId);
            }
            ChangeList changeList = request.execute();
            return changeList.getItems();
        } catch (IOException e) {
            throw new SynchronizationException("Error occurred while getting file changes from Google Drive", e);
        }
    }

    public List<ServerEntry> getFileList() throws IOException, SynchronizationException {
        List<ServerEntry> result = new ArrayList<>();
        List<Change> remotes = new ArrayList<>();
        Drive.Changes.List request = getClient().changes().list();
        do {
            ChangeList files = request.execute();
            remotes.addAll(files.getItems());
            for (Change change : remotes) {
                com.google.api.services.drive.model.File remoteFile = change.getFile();
                if (!remoteFile.getTitle().equals(EXCLUDE_FILE)) {
                    final DriveServerEntry serverEntry = createDriveServerEntry(remoteFile);
                    result.add(serverEntry);
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && !request.getPageToken().isEmpty());

        return result;
    }

    @Override
    public void download(final ServerEntry serverEntry, final FileOutputStream outputStream) throws SynchronizationException {
        DriveServerEntry driveServerEntry = (DriveServerEntry) serverEntry;
        com.google.api.services.drive.model.File remoteFile = driveServerEntry.getRemoteFile();
        String downloadUrl = remoteFile.getDownloadUrl();
        if (downloadUrl != null && !downloadUrl.isEmpty()) {
            try {
                HttpResponse resp = getClient().getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl)).execute();
                InputStream inputStream = resp.getContent();
                writeToFile(inputStream, outputStream);

            } catch (IOException e) {
                LOG.error("Failed to write downloaded file to disc: {}", serverEntry.getPath());
                throw new SynchronizationException("Failed to write to disc: " + serverEntry);
            }
        } else {
            // The remoteFile doesn't have any content stored on Drive.
        }

    }

    public void writeToFile(final InputStream stream, final FileOutputStream outStream) throws IOException {
        FileChannel outChannel = outStream.getChannel();
        ReadableByteChannel inChannel = Channels.newChannel(stream);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (inChannel.read(buffer) >= 0 || buffer.position() > 0) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.compact();
        }

        inChannel.close();
        outChannel.close();
    }

    @Override
    public DriveServerEntry download(final String serverPath, final FileOutputStream outputStream, final Object... extraArgs) throws SynchronizationException {
        throw new UnsupportedOperationException("Use the public void download(ServerEntry serverEntry, FileOutputStream outputStream) method.");
    }

    @Override
    public void delete(final Path remoteFilePath) throws SynchronizationException {
        throw new UnsupportedOperationException("Delete operation is only available with file id");
    }

    @Override
    public void delete(final String remoteFileId) throws SynchronizationException {
        try {
            getClient().files().delete(remoteFileId).execute();
        } catch (IOException e) {
            throw new SynchronizationException(String.format("Failed to delete file (id=%s) from Google Drive", remoteFileId));
        }
    }

    @Override
    public ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException {
        final File fileToUpload = uploadEntity.getFile();
        final List<ParentReference> parentReferences = getParentDirectories(uploadEntity.getRemoteFolder());
        final com.google.api.services.drive.model.File content = getContent(uploadEntity.getFile(), parentReferences);
        final FileContent mediaContent = new FileContent("text/plain", fileToUpload);
        com.google.api.services.drive.model.File driveFile;
        try {
            driveFile = getClient().files().insert(content, mediaContent).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive new remoteFile upload failed", e);
        }

        Date lastModified = new Date(driveFile.getModifiedDate().getValue());
        return new DriveServerEntry(uploadEntity.getRemoteFilePath().toString(), driveFile.getHeadRevisionId(), lastModified, false);
    }

    private List<ParentReference> getParentDirectories(final Path remotePath) throws SynchronizationException {
        List<ParentReference> parentReferences;
        try {
            parentReferences = dirUtil.createFoldersPath(remotePath.toString().split("/"));
        } catch (IOException e) {
            throw new SynchronizationException("Remote folder not found", e);
        }
        return parentReferences;
    }

    @Override
    public ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        final File fileToUpload = uploadEntity.getFile();
        final String driveFileId = getFileId(fileToUpload);
        final com.google.api.services.drive.model.File driveFile = getDriveFile(driveFileId);
        final FileContent mediaContent = new FileContent(driveFile.getMimeType(), fileToUpload);

        com.google.api.services.drive.model.File updatedFile;
        try {
            updatedFile = getClient().files().update(driveFileId, driveFile, mediaContent).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive remote file update failed", e);
        }

        Date lastModified = new Date(updatedFile.getModifiedDate().getValue());
        return new DriveServerEntry(uploadEntity.getRemoteFilePath().toString(), updatedFile.getHeadRevisionId(), lastModified, false);
    }

    @Override
    public List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException {
        final List<ServerEntry> result = new ArrayList<>();
        try {
            final String folderId = dirUtil.getId(remoteFolder);
            final FileList fileList = getClient().files().list().setQ(String.format("'%s' in parents AND trashed = false", folderId)).execute();
            for (com.google.api.services.drive.model.File remoteFile : fileList.getItems()) {
                if (!remoteFile.getTitle().equals(EXCLUDE_FILE)) {
                    DriveServerEntry serverEntry = createDriveServerEntry(remoteFile);
                    result.add(serverEntry);
                }
            }
        } catch (IOException e) {
            throw new SynchronizationException(String.format("%s: %s", GoogleDrive.name, e.getMessage()));
        }
        return result;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        final List<ServerEntry> result = new ArrayList<>();
        final List<com.google.api.services.drive.model.File> remotes = new ArrayList<>();
        try {
            final FileList fileList = getClient().files().list().setQ("'root' in parents AND trashed=false").execute();
            remotes.addAll(fileList.getItems());
            for (com.google.api.services.drive.model.File remoteFile : remotes) {
                if (!remoteFile.getTitle().equals(EXCLUDE_FILE)) {
                    DriveServerEntry serverEntry = createDriveServerEntry(remoteFile);
                    result.add(serverEntry);
                }
            }
        } catch (IOException e) {
            throw new SynchronizationException(String.format("%s: %s", GoogleDrive.name, e.getMessage()));
        }
        return result;
    }

    @Override
    public void createFolder(final Path folder) throws SynchronizationException {
        final List<String> folders = new ArrayList<>(folder.getNameCount());
        for (int i = 0; i < folder.getNameCount(); i++) {
            folders.add(folder.getName(i).toString());
        }
        try {
            dirUtil.createFoldersPath(folders);
        } catch (IOException e) {
            throw new SynchronizationException("folder creation failed", e);
        }
    }

    @Override
    public AccountQuota getQuota() throws SynchronizationException {
        try {
            About about = getClient().about().get().execute();
            return new AccountQuota(about.getQuotaBytesTotal(), about.getQuotaBytesUsedAggregate());
        } catch (IOException e) {
            throw new SynchronizationException("Could not get Drive storage quota", e);
        }
    }

    @Override
    public FileEntity rename(final FileEntity remoteFile, final String newName) throws SynchronizationException {
        try {
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(newName);

            Drive.Files.Patch patchRequest = getClient().files().patch(remoteFile.getId(), file);
            patchRequest.setFields("title");

            com.google.api.services.drive.model.File updatedFile = patchRequest.execute();
            return new DriveFileEntity(remoteFile.getLocalPath(), remoteFile.getRemotePath(), updatedFile.getHeadRevisionId(), new Date(updatedFile.getModifiedDate().getValue()));
        } catch (IOException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getRemotePath()), e);
        }
    }

    @Override
    public FileEntity rename(final ServerEntry remoteFile, final String newName) throws SynchronizationException {
        try {
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(newName);

            Drive.Files.Patch patchRequest = getClient().files().patch(remoteFile.getId(), file);
            patchRequest.setFields("title");

            com.google.api.services.drive.model.File updatedFile = patchRequest.execute();
            return new DriveFileEntity(null, remoteFile.getPath().toString(), updatedFile.getHeadRevisionId(), new Date(updatedFile.getModifiedDate().getValue()));
        } catch (IOException e) {
            LOG.error(String.format("%s file rename failed", getAccountName()), e);
            throw new SynchronizationException(String.format("An error occurred while rename file %s", remoteFile.getPath()), e);
        }
    }

    private com.google.api.services.drive.model.File getDriveFile(final String driveFileId) throws SynchronizationException {
        com.google.api.services.drive.model.File driveFile;
        try {
            driveFile = getClient().files().get(driveFileId).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive remoteFile update failed", e);
        }
        return driveFile;
    }

    private String getFileId(final File fileToUpload) {
        DriveFileEntity driveFileEntity = (DriveFileEntity) persistenceController.get(fileToUpload.getPath(), DriveFileEntity.class);
        return driveFileEntity.getFileId();
    }

    private com.google.api.services.drive.model.File getContent(final File file, final List<ParentReference> parentReferences) {
        com.google.api.services.drive.model.File content = new com.google.api.services.drive.model.File();
        content.setTitle(file.getName());
        content.setMimeType("text/plain");
        content.setParents(parentReferences);
        return content;
    }

    private DriveServerEntry createDriveServerEntry(final com.google.api.services.drive.model.File remoteFile) throws SynchronizationException {
        final String path = dirUtil.getPath(remoteFile);
        final String revision = remoteFile.getHeadRevisionId();
        final Date lastModified = new Date(remoteFile.getModifiedDate().getValue());
        return new DriveServerEntry(remoteFile, path, revision, lastModified, dirUtil.isFolder(remoteFile));
    }

}
