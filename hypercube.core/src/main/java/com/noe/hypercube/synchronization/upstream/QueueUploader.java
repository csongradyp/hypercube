package com.noe.hypercube.synchronization.upstream;


import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.FileEntityFactory;
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

import static com.noe.hypercube.synchronization.Action.*;

public class QueueUploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> extends Uploader<ACCOUNT_TYPE, ENTITY_TYPE> {

    private static final Logger LOG = LoggerFactory.getLogger(QueueUploader.class);

    private final BlockingQueue<UploadEntity> uploadQ;
    private boolean stop = false;

    public QueueUploader(IClient client, FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> persistenceController, IPersistenceController entityFactory) {
        super(client, entityFactory, persistenceController);
        uploadQ = new LinkedBlockingDeque<>(20);
    }

    @Override
    public void run() {
        while(!stop) {
            UploadEntity uploadEntity = uploadQ.poll();
            Path remotePath = uploadEntity.getRemotePath();
            File file = uploadEntity.getFile();
            try {
                switch(uploadEntity.getAction()) {
                    case ADDED:
                        super.uploadNew(file, remotePath);
                        break;
                    case CHANGED:
                        super.uploadUpdated(file, remotePath);
                        break;
                    case REMOVED:
                        super.delete(file, remotePath);
                        break;
                }
            }catch (SynchronizationException e) {
                LOG.error(e.getMessage(), e);
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
    public void uploadNew(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, ADDED);
    }

    @Override
    public void uploadUpdated(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, CHANGED);
    }

    @Override
    public void delete(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, REMOVED);
    }

    public void submit(final File file, final Path remotePath, final Action action) throws SynchronizationException {
        uploadQ.add(new UploadEntity(file, remotePath, action));
    }
}
