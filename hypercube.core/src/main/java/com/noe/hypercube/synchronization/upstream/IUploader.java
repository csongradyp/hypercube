package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.nio.file.Path;

public interface IUploader<T> {

    void uploadNew(File file, Path remotePath) throws SynchronizationException;

    void uploadUpdated(File file, Path remotePath) throws SynchronizationException;

    void delete(File file, Path remotePath) throws SynchronizationException;

}
