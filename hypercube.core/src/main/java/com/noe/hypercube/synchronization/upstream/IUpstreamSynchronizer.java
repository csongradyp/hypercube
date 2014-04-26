package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.nio.file.Path;

public interface IUpstreamSynchronizer<ACCOUNT_TYPE> {

    void submit(File file, Path remotePath, Action action) throws SynchronizationException;

    void submitNew(File file, Path remotePath) throws SynchronizationException;

    void submitChanged(File file, Path remotePath) throws SynchronizationException;

    void submitDelete(File file, Path remotePath) throws SynchronizationException;
}
