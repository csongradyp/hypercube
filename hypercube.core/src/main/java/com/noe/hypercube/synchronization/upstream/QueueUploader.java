package com.noe.hypercube.synchronization.upstream;


import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.noe.hypercube.synchronization.Action.ADDED;
import static com.noe.hypercube.synchronization.Action.CHANGED;
import static com.noe.hypercube.synchronization.Action.REMOVED;

public class QueueUploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> extends Uploader<ACCOUNT_TYPE, ENTITY_TYPE> {

    private static final Logger LOG = LoggerFactory.getLogger(QueueUploader.class);

    private final BlockingQueue<UploadEntity> uploadQ;
    private boolean stop = false;

    public QueueUploader(IClient<ACCOUNT_TYPE, ENTITY_TYPE, ? extends MappingEntity> client, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> persistenceController, IPersistenceController entityFactory) {
        super(client, entityFactory, persistenceController);
        uploadQ = new LinkedBlockingDeque<>(100);
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                final UploadEntity uploadEntity = uploadQ.take();
                final Action action = uploadEntity.getAction();
                if (ADDED == action) {
                    super.uploadNew(uploadEntity);
                } else if (CHANGED == action) {
                    super.uploadUpdated(uploadEntity);
                } else if (REMOVED == action) {
                    super.delete(uploadEntity);
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOG.error(String.format("%s upload queue reading  has been interrupted", client.getAccountName()));
            }
        }
    }

    public void stop() {
        stop = true;
    }

    public void restart() {
        stop = false;
        run();
    }

    @Override
    public void uploadUpdated(final UploadEntity uploadEntity) throws SynchronizationException {
        uploadQ.add(uploadEntity);
    }

    @Override
    public void uploadNew(final UploadEntity uploadEntity) throws SynchronizationException {
        uploadQ.add(uploadEntity);
    }

    public void submit(final File file, final Path remotePath, final String origin) throws SynchronizationException {
        uploadQ.add(new UploadEntity(file, remotePath, origin));
    }
}
