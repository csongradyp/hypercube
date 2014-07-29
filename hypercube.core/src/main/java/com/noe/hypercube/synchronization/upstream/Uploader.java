package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.FileEventType;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static com.noe.hypercube.synchronization.Action.ADDED;
import static com.noe.hypercube.synchronization.Action.CHANGED;
import static com.noe.hypercube.synchronization.Action.REMOVED;
import static java.lang.String.format;

public abstract class Uploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements IUploader<ACCOUNT_TYPE, ENTITY_TYPE> {

    private static final Logger LOG = LoggerFactory.getLogger(Uploader.class);

    protected final IPersistenceController persistenceController;
    protected final IClient<ACCOUNT_TYPE, ENTITY_TYPE> client;
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
    public void uploadNew(final UploadEntity uploadEntity) throws SynchronizationException {
        final File fileToUpload = uploadEntity.getFile();
        final Path remoteFilePath = uploadEntity.getRemoteFolder();
        if (client.exist(uploadEntity)) {
            LOG.debug("{} conflict - File already exists on server: {}", client.getAccountName(), remoteFilePath.toString());
            uploadEntity.setConflicted(true);
        }
        upload(uploadEntity, ADDED);
    }

    @Override
    public void uploadUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        final File fileToUpload = uploadEntity.getFile();
        final Path remoteFolder = uploadEntity.getRemoteFolder();
        if(client.exist(uploadEntity) && isNewer(fileToUpload)) {
            upload(uploadEntity, CHANGED);
        }
        else {
            LOG.debug("{} inconsistency - Remote file '{}' is fresher than the local one: {}", client.getAccountName(), remoteFolder.toString(), fileToUpload.toPath());
        }
    }

    private void upload(final UploadEntity uploadEntity, Action action) throws SynchronizationException {
        final Path localPath = uploadEntity.getFile().toPath();
        ServerEntry uploadedFile = null;
        try {
            if (REMOVED != action) {
                final Path remoteFilePath = uploadEntity.getRemoteFilePath();
                if (CHANGED == action) {
                    EventBus.publish(new FileEvent(localPath, remoteFilePath, FileEventType.UPDATED));
                    uploadedFile = client.uploadAsUpdated(uploadEntity);
                } else if (ADDED == action) {
                    EventBus.publish(new FileEvent(localPath, remoteFilePath, FileEventType.NEW));
                    uploadedFile = client.uploadAsNew(uploadEntity);
                }
            }
            if(uploadedFile == null) {
                throw new SynchronizationException(format("Upload failed - Cannot upload file: '%s' to %s", localPath.toString(), client.getAccountName()));
            }
            persist(localPath, uploadedFile);
            LOG.debug("successfully uploaded file: '{}' with new revision: {}", uploadedFile.getPath(), uploadedFile.getRevision());
        } catch (IOException e) {
            throw new SynchronizationException(String.format("Upload failed - Cannot read file: %s", localPath.toString()), e);
        }
    }

    private void persist(final Path localPath, final ServerEntry uploadedFile) throws IOException {
        FileEntity fileEntity = entityFactory.createFileEntity(localPath.toString(), uploadedFile.getRevision(), uploadedFile.lastModified());
        persistenceController.save(fileEntity);
    }

    @Override
    public void delete(final UploadEntity uploadEntity) throws SynchronizationException {
        final File localFile = uploadEntity.getFile();
        final Path remotePath = uploadEntity.getRemoteFolder();
        if(client.exist(uploadEntity)) {
            final Path localPath = localFile.toPath();
            client.delete(localFile, remotePath);
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
