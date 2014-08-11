package com.noe.hypercube.googledrive.service;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.ParentReference;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.googledrive.domain.DriveFileEntity;
import com.noe.hypercube.googledrive.domain.DriveServerEntry;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

public class DriveClient implements IClient<GoogleDrive, DriveFileEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(DriveClient.class);
    private static final String EXCLUDE_FILE = "collaboration";

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private DriveDirectoryUtil dirUtil;

    private final Drive client;

    public DriveClient(Drive client) {
        this.client = client;
    }

    @Override
    public String getAccountName() {
        return "Google Drive";
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
    public boolean exist(final File fileToUpload, final Path remotePath) {
        DriveFileEntity fileEntity = (DriveFileEntity) persistenceController.get(fileToUpload.getPath(), DriveFileEntity.class);
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
            file = client.files().get(fileId).execute();
        } catch (IOException e) {

        }
        return file != null;
    }

    @Override
    public Collection<ServerEntry> getChanges() throws SynchronizationException {
        try {
            return getFileList();
        } catch (IOException e) {
            throw new SynchronizationException("An error while getting file list from Google Drive Server ", e);
        }
    }

    public List<ServerEntry> getFileList() throws IOException {
        List<ServerEntry> result = new ArrayList<>();
        List<Change> remotes = new ArrayList<>();
        Drive.Changes.List request = client.changes().list();
        do {
            ChangeList files = request.execute();
            remotes.addAll(files.getItems());
            for (Change change : remotes) {
                com.google.api.services.drive.model.File remoteFile = change.getFile();
                if(!remoteFile.getTitle().equals(EXCLUDE_FILE)){
                    DriveServerEntry serverEntry = new DriveServerEntry(remoteFile, dirUtil.getPath(remoteFile),remoteFile.getHeadRevisionId(), new Date(remoteFile.getModifiedDate().getValue()));
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
                HttpResponse resp = client.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl)).execute();
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
    public void download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException {
        throw new UnsupportedOperationException("Use the public void download(ServerEntry serverEntry, FileOutputStream outputStream) method.");
    }

    @Override
    public void delete(final File localFile, final Path remotePath) throws SynchronizationException {
        String fileId = getFileId(localFile);
        try {
            client.files().delete(fileId);
        } catch (IOException e) {
            LOG.error("Google Drive file deletion failed from server: {}", remotePath);
            throw new SynchronizationException("Google Drive file deletion failed from server: " + remotePath);
        }
    }

    @Override
    public ServerEntry uploadAsNew(final Path remotePath, final File fileToUpload, final InputStream inputStream) throws SynchronizationException {
        List<ParentReference> parentReferences = getParentDirectories(remotePath);
        com.google.api.services.drive.model.File content = getContent(fileToUpload, parentReferences);
        FileContent mediaContent = new FileContent("text/plain", fileToUpload);
        com.google.api.services.drive.model.File driveFile;
        try {
            driveFile = client.files().insert(content, mediaContent).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive new remoteFile upload failed", e);
        }

        Date lastModified = new Date(driveFile.getModifiedDate().getValue());
        return new DriveServerEntry(remotePath.toString(), driveFile.getHeadRevisionId(), lastModified, false);
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
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException {
        String driveFileId = getFileId(fileToUpload);
        com.google.api.services.drive.model.File driveFile = getDriveFile(driveFileId);
        FileContent mediaContent = new FileContent(driveFile.getMimeType(), fileToUpload);

        com.google.api.services.drive.model.File updatedFile;
        try {
            updatedFile = client.files().update(driveFileId, driveFile, mediaContent).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive remoteFile update failed", e);
        }

        Date lastModified = new Date(updatedFile.getModifiedDate().getValue());
        return new DriveServerEntry(remotePath.toString(), updatedFile.getHeadRevisionId(), lastModified, false);
    }

    @Override
    public List<ServerEntry> getFileList(Path remoteFolder) throws SynchronizationException {
        // TODO implement
        return null;
    }

    @Override
    public List<ServerEntry> getRootFileList() throws SynchronizationException {
        return null;
    }

    private com.google.api.services.drive.model.File getDriveFile(String driveFileId) throws SynchronizationException {
        com.google.api.services.drive.model.File driveFile;
        try {
            driveFile = client.files().get(driveFileId).execute();
        } catch (IOException e) {
            throw new SynchronizationException("Drive remoteFile update failed", e);
        }
        return driveFile;
    }

    private String getFileId(File fileToUpload) {
        DriveFileEntity driveFileEntity = (DriveFileEntity) persistenceController.get(fileToUpload.getPath(), DriveFileEntity.class);
        return driveFileEntity.getFileId();
    }

    private com.google.api.services.drive.model.File getContent(java.io.File file, List<ParentReference> parentReferences) {
        com.google.api.services.drive.model.File content = new com.google.api.services.drive.model.File();
        content.setTitle(file.getName());
        content.setMimeType("text/plain");
        content.setParents(parentReferences);
        return content;
    }
}
