package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static java.lang.String.format;

public abstract class Uploader<ENTITY_TYPE> implements IUploader {

    private static final Logger LOG = LoggerFactory.getLogger(Uploader.class);

    private final IClient client;
    private final IPersistenceController persistenceController;

    protected Uploader(final IClient client, final IPersistenceController persistenceController) {
        this.client = client;
        this.persistenceController = persistenceController;
    }

    public abstract Class<ENTITY_TYPE> getEntityClass();

    @Override
    public void uploadNew(File fileToUpload, Path remotePath) throws SynchronizationException {
        if(!client.exist(remotePath)) {
            upload(fileToUpload, remotePath, Action.ADDED);
        }
        else {
            LOG.debug("{} conflict - File already exists on server: {}", client.getAccountName(), remotePath.toString());
        }
    }
    @Override
    public void uploadUpdated(File fileToUpload, Path remotePath) throws SynchronizationException {
        if(client.exist(remotePath) && isNewer(fileToUpload)) {
            upload(fileToUpload, remotePath, Action.CHANGED);
        }
        else {
            LOG.debug("{} inconsistency - Remote file '{}' is fresher than the local one: {}", client.getAccountName(), remotePath.toString(), fileToUpload.toPath());
        }
    }

    private synchronized void upload(File fileToUpload, Path remotePath, Action action) throws SynchronizationException {
        Path localPath = fileToUpload.toPath();
        ServerEntry uploadedFile = null;
        try(FileInputStream inputStream = FileUtils.openInputStream(fileToUpload)) {
            switch(action) {
                case CHANGED:
                    uploadedFile = client.uploadAsUpdated(remotePath, fileToUpload, inputStream);
                    break;
                case ADDED:
                    uploadedFile = client.uploadAsNew(remotePath, fileToUpload, inputStream);
                    break;
            }
            if(uploadedFile == null) {
                throw new SynchronizationException(format("Upload failed - Cannot upload file: '%s' to %s", localPath.toString(), client.getAccountName()));                }
            persist(localPath, uploadedFile, action);
            LOG.debug("successfully uploaded file: '{}' with new revision: {}", uploadedFile.getPath(), uploadedFile.getRevision());
        } catch (IOException e) {
            throw new SynchronizationException("Upload failed - Cannot read file: " + localPath.toString(), e);
        }
    }

    private synchronized void upload(Path remotePath, Path localPath, Action action) throws SynchronizationException {
        File fileToUpload = new File(localPath.toUri());
        ServerEntry uploadedFile = null;
        try(FileInputStream inputStream = FileUtils.openInputStream(fileToUpload)) {
            switch(action) {
                case CHANGED:
                    uploadedFile = client.uploadAsUpdated(remotePath, fileToUpload, inputStream);
                    break;
                case ADDED:
                    uploadedFile = client.uploadAsNew(remotePath, fileToUpload, inputStream);
                    break;
            }
            if(uploadedFile == null) {
                throw new SynchronizationException(format("Upload failed - Cannot upload file: '%s' to %s", localPath.toString(), client.getAccountName()));                }
            persist(localPath, uploadedFile, action);
            LOG.debug("successfully uploaded file: '{}' with new revision: {}", uploadedFile.getPath(), uploadedFile.getRevision());
        } catch (IOException e) {
            throw new SynchronizationException("Upload failed - Cannot read file: " + localPath.toString(), e);
        }
    }

    protected abstract FileEntity createFileEntity(String localPath, String revision, Date date);

    private void persist(Path localPath, ServerEntry uploadedFile, Action action) throws IOException {
        FileEntity fileEntity = createFileEntity(localPath.toString(), uploadedFile.getRevision(), uploadedFile.lastModified());
        persistenceController.save(fileEntity);
    }

    @Override
    public synchronized void delete(File fileToUpload, Path remotePath) throws SynchronizationException {
        if(client.exist(remotePath)) {
            client.delete(remotePath);
            Path localPath = Paths.get(fileToUpload.toURI());
            persistenceController.delete(localPath.toString(), client.getEntityClass());
            LOG.debug("Successfully deleted file '{}' from {}", remotePath, client.getAccountName());
        }
        else {
            LOG.debug("{} already deleted from {}", remotePath, client.getAccountName());
        }
    }

    private boolean isNewer(File fileToUpload) {
        Path localPath = fileToUpload.toPath();
        FileEntity dbEntry = persistenceController.get(localPath.toString(), client.getEntityClass());
        if(dbEntry == null) {
            return true;
        }
        Date dbLastModifiedDate = dbEntry.lastModified();
        Date localLastModified = new Date(fileToUpload.lastModified());
        return dbLastModifiedDate.before(localLastModified);
    }

}
