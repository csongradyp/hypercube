package com.noe.hypercube.service;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;

public interface IClient<ENTITY_TYPE extends FileEntity> {

    /**
     * Returns the official name of the file hosting service.
     * E.g.: Dropbox
     */
    String getAccountName();

    /**
     * Returns the account specific <code>Class</code> which is annotated with @Entity.
     */
    Class<ENTITY_TYPE> getEntityClass();

    /**
     * Checks if the file exists on the server for the given server specific path.
     * @return {@code true} if the file exists on the server in the given path.
     */
    boolean exist(Path remotePath);

    boolean exist(ServerEntry serverEntry);

    Collection<ServerEntry> getChanges()  throws SynchronizationException;

    void download(ServerEntry serverPath, OutputStream outputStream)  throws SynchronizationException;;

    void download(String serverPath, OutputStream outputStream, Object... extraArgs)  throws SynchronizationException;;

    void delete(Path remotePath) throws SynchronizationException;

    ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException;

    ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream) throws SynchronizationException;

}
