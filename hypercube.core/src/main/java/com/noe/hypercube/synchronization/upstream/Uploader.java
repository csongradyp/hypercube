package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.Action;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.FileActionType;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.noe.hypercube.Action.*;
import static java.lang.String.format;

public abstract class Uploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> implements IUploader<ACCOUNT_TYPE, ENTITY_TYPE> {

    private static final Logger LOG = LoggerFactory.getLogger(Uploader.class);

    protected final IClient<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> client;
    private final IPersistenceController persistenceController;
    private final FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory;

    protected Uploader(IClient<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> client, IPersistenceController persistenceController, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> entityFactory) {
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
        final Path remoteFilePath = uploadEntity.getRemoteFilePath();
        if (isMapped(uploadEntity.getFile())) {
            LOG.info("file {} already mapped - cannot be new. Upload cancelled", uploadEntity.getFile());
        } else {
            if (client.exist(uploadEntity)) {
                LOG.error("{} conflict - File already exists on server: {}", client.getAccountName(), remoteFilePath.toString());
                uploadEntity.setConflicted(true);
            }
            upload(uploadEntity, ADDED);
        }
    }

    private boolean isMapped(final File file) {
        return persistenceController.get(file.toPath().toString(), getEntityType()) != null;
    }

    @Override
    public void uploadUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        final File fileToUpload = uploadEntity.getFile();
        final Path remoteFolder = uploadEntity.getRemoteFolder();
        if (client.exist(uploadEntity) && isNewer(fileToUpload)) {
            upload(uploadEntity, CHANGED);
        } else {
            LOG.debug("{} inconsistency - Remote file in folder '{}' is fresher than the local one: {}", client.getAccountName(), remoteFolder.toString(), fileToUpload.toPath());
        }
    }

    private void upload(final UploadEntity uploadEntity, final Action action) throws SynchronizationException {
        final Path localPath = uploadEntity.getFile().toPath();
        final Path remotePath = uploadEntity.getRemoteFilePath();
        final String accountName = client.getAccountName();
        ServerEntry uploadedFile = null;
        try {
            if (REMOVED != action) {
                if (CHANGED == action) {
                    final FileEvent event = new FileEvent(accountName, localPath, remotePath, FileActionType.UPDATED);
                    EventBus.publishUploadStart(event);
                    uploadedFile = client.uploadAsUpdated(uploadEntity);
                    EventBus.publishUploadFinished(event);
                } else if (ADDED == action) {
                    final FileEvent event = new FileEvent(accountName, localPath, remotePath, FileActionType.ADDED);
                    EventBus.publishUploadStart(event);
                    uploadedFile = client.uploadAsNew(uploadEntity);
                    EventBus.publishUploadFinished(event);
                }
            }
            if (uploadedFile == null) {
                throw new SynchronizationException(format("Upload failed - Cannot upload file: '%s' to %s", localPath.toString(), client.getAccountName()));
            }
            persist(localPath, uploadedFile);
            LOG.debug("successfully uploaded file: '{}' with new revision: {}", uploadedFile.getPath(), uploadedFile.getRevision());
        } catch (IOException e) {
            throw new SynchronizationException(String.format("Upload failed - Cannot read file: %s", localPath.toString()), e);
        }
    }


    private void persist(final Path localPath, final ServerEntry uploadedFile) throws IOException {
        final FileEntity fileEntity = entityFactory.createFileEntity(localPath.toString(), uploadedFile.getPath().toString(), uploadedFile.getRevision(), uploadedFile.lastModified());
        persistenceController.save(fileEntity);
        persistenceController.save(new LocalFileEntity(localPath.toFile()));
    }

    @Override
    public void delete(final UploadEntity uploadEntity) throws SynchronizationException {
        final File localFile = uploadEntity.getFile();
        final Path remotePath = uploadEntity.getRemoteFolder();
        if (client.exist(uploadEntity)) {
            final Path localPath = localFile.toPath();
            client.delete(uploadEntity.getRemoteFilePath());
            persistenceController.delete(localPath.toString(), client.getEntityType());
            LOG.debug("Successfully deleted file '{}' from {}", remotePath, client.getAccountName());
        } else {
            LOG.debug("{} already deleted from {}", remotePath, client.getAccountName());
        }
    }

    private boolean isNewer(final File fileToUpload) {
        final Path localPath = fileToUpload.toPath();
        final FileEntity dbEntry = persistenceController.get(localPath.toString(), client.getEntityType());
        if (dbEntry == null) {
            return true;
        }
        final Date dbLastModifiedDate = dbEntry.lastModified();
        final Date localLastModified = new Date(fileToUpload.lastModified());
        return dbLastModifiedDate.before(localLastModified);
    }

}
