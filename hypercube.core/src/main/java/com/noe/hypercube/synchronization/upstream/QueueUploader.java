package com.noe.hypercube.synchronization.upstream;


import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.service.AccountType;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public abstract class QueueUploader<ACCOUNT_TYPE extends AccountType> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(QueueUploader.class);

    private BlockingQueue<UploadEntity> queue;
    private IUploader<ACCOUNT_TYPE, ? extends FileEntity> uploader;
    private boolean stop = false;

    public QueueUploader(IUploader<ACCOUNT_TYPE, ? extends FileEntity> uploader, BlockingQueue<UploadEntity> queue) {
        this.uploader = uploader;
        this.queue = queue;
    }

    public abstract Class<ACCOUNT_TYPE> getAccountType();

    @Override
    public void run() {
        while(!stop) {
            UploadEntity uploadEntity = queue.poll();
            Path remotePath = uploadEntity.getRemotePath();
            File file = uploadEntity.getFile();
            try {
                switch(uploadEntity.getAction()) {
                    case ADDED:
                        uploader.uploadNew(file, remotePath);
                        break;
                    case CHANGED:
                        uploader.uploadUpdated(file, remotePath);
                        break;
                    case REMOVED:
                        uploader.delete(file, remotePath);
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

    public void submit(File file, Path remotePath, Action action) throws SynchronizationException {
        new UploadEntity(file, remotePath, action);
    }
}
