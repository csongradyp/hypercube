package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.FileActionType;
import com.noe.hypercube.service.Account;
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
import java.util.Date;

import static com.noe.hypercube.synchronization.Action.*;
import static java.lang.String.format;

public abstract class Uploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements IUploader<ACCOUNT_TYPE, ENTITY_TYPE> {

    private static final Logger LOG = LoggerFactory.getLogger(Uploader.class);

    protected final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
    private final IPersistenceController persistenceController;
    private final FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory;

    protected Uploader(IClient<ACCOUNT_TYPE, ENTITY_TYPE> client, IPersistenceController persistenceController, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory) {
        this.persistenceController = persistenceController;
        this.client = client;
        this.entityFactory = entityFactory;
    }

    @Override
    public Class<ACCOUNT_TYPE> getAccountType() {
        return client.getAccountType();
    }

    @Override
    public Class<ENTITY_TYPE> getEntityType() {
        return client.getEntityType();
    }

    @Override
    public void uploadNew(File fileToUpload, Path remotePath) throws SynchronizationException {
        if (client.exist(fileToUpload, remotePath)) {
            LOG.debug("{} conflict - File already exists on server: {}", client.getAccountName(), remotePath.toString());
        } else {
            upload(fileToUpload, remotePath, ADDED);
        }
    }

    @Override
    public void uploadUpdated(File fileToUpload, Path remotePath) throws SynchronizationException {
        if(client.exist(fileToUpload, remotePath) && isNewer(fileToUpload)) {
            upload(fileToUpload, remotePath, CHANGED);
        }
        else {
            LOG.debug("{} inconsistency - Remote file '{}' is fresher than the local one: {}", client.getAccountName(), remotePath.toString(), fileToUpload.toPath());
        }
    }

    private synchronized void upload(File fileToUpload, Path remotePath, Action action) throws SynchronizationException {
        final Path localPath = fileToUpload.toPath();
        ServerEntry uploadedFile = null;
        try(FileInputStream inputStream = FileUtils.openInputStream(fileToUpload)) {
            final String accountName = client.getAccountName();
            if (REMOVED != action) {
                if (CHANGED == action) {
                    final FileEvent event = new FileEvent(accountName, localPath, remotePath, FileActionType.UPDATED);
                    EventBus.publishUploadStart(event);
                    uploadedFile = client.uploadAsUpdated(remotePath, fileToUpload, inputStream);
                    EventBus.publishUploadFinished(event);
                } else if (ADDED == action) {
                    final FileEvent event = new FileEvent(accountName, localPath, remotePath, FileActionType.ADDED);
                    EventBus.publishUploadStart(event);
                    uploadedFile = client.uploadAsNew(remotePath, fileToUpload, inputStream);
                    EventBus.publishUploadFinished(event);
                }
            }
            if(uploadedFile == null) {
                throw new SynchronizationException(format("Upload failed - Cannot upload file: '%s' to %s", localPath.toString(), accountName));
            }
            persist(localPath, uploadedFile);
            LOG.debug("successfully uploaded file: '{}' with new revision: {}", uploadedFile.getPath(), uploadedFile.getRevision());
        } catch (IOException e) {
            throw new SynchronizationException("Upload failed - Cannot read file: " + localPath.toString(), e);
        }
    }

    private void persist(Path localPath, ServerEntry uploadedFile) throws IOException {
        FileEntity fileEntity = entityFactory.createFileEntity(localPath.toString(), uploadedFile.getRevision(), uploadedFile.lastModified());
        persistenceController.save(fileEntity);
    }

    @Override
    public synchronized void delete(File localFile, Path remotePath) throws SynchronizationException {
        if(client.exist(localFile, remotePath)) {
            client.delete(localFile, remotePath);
            Path localPath = localFile.toPath();
            persistenceController.delete(localPath.toString(), client.getEntityType());
            LOG.debug("Successfully deleted file '{}' from {}", remotePath, client.getAccountName());
        }
        else {
            LOG.debug("{} already deleted from {}", remotePath, client.getAccountName());
        }
    }

    private boolean isNewer(File fileToUpload) {
        Path localPath = fileToUpload.toPath();
        FileEntity dbEntry = persistenceController.get(localPath.toString(), client.getEntityType());
        if(dbEntry == null) {
            return true;
        }
        Date dbLastModifiedDate = dbEntry.lastModified();
        Date localLastModified = new Date(fileToUpload.lastModified());
        return dbLastModifiedDate.before(localLastModified);
    }

}
