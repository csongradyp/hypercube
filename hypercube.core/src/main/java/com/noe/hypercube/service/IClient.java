package com.noe.hypercube.service;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

public interface IClient<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity>{

    /**
     * Returns the official name of the file hosting service.
     * E.g.: Dropbox
     */
    String getAccountName();

    /**
     * Returns the account specific <code>Class</code> which is annotated with @Entity.
     */
    Class<ENTITY_TYPE> getEntityType();

    Class<ACCOUNT_TYPE> getAccountType();

    /**
     * Checks whether the file exists on the server or not.
     * @return {@code true} if the file exists on the server in the given path in the {@link com.noe.hypercube.domain.ServerEntry} instance.
     */
    boolean exist(final ServerEntry serverEntry) throws SynchronizationException;
    /**
     * Checks whether the file exists on the server or not.
     * @return {@code true} if the file exists on the server in the given path in the {@link com.noe.hypercube.domain.UploadEntity} instance.
     */
    boolean exist(final UploadEntity uploadEntity) throws SynchronizationException;

    Collection<ServerEntry> getChanges() throws SynchronizationException;

    void download(final ServerEntry serverPath, final FileOutputStream outputStream) throws SynchronizationException;

    void download(final String serverPath, final FileOutputStream outputStream, final Object... extraArgs) throws SynchronizationException;

    void delete(final File fileToUpload, final Path remoteFolder) throws SynchronizationException;

    ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException;

    ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException;
}
