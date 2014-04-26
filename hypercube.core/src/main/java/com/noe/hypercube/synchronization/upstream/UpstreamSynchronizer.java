package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import static com.noe.hypercube.synchronization.Action.*;

public class UpstreamSynchronizer<ACCOUNT_TYPE extends Account> implements IUpstreamSynchronizer<ACCOUNT_TYPE> {

    private BlockingQueue<UploadEntity> queue;

    public UpstreamSynchronizer(final BlockingQueue<UploadEntity> queue) {
        this.queue = queue;
    }

    @Override
    public void submitNew(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, ADDED);
    }

    @Override
    public void submitChanged(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, CHANGED);
    }

    @Override
    public void submitDelete(final File file, final Path remotePath) throws SynchronizationException {
        submit(file, remotePath, REMOVED);
    }

    @Override
    public void submit(final File file, final Path remotePath, final Action action) throws SynchronizationException {
        queue.add(new UploadEntity(file, remotePath, action));
    }
}
