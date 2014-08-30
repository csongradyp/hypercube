package com.noe.hypercube.service;

import com.noe.hypercube.domain.AccountQuota;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface IClient<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> {

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
     * Checks if the file exists on the server for the given server specific path.
     * @return {@code true} if the file exists on the server in the given path.
     */
    boolean exist(final File fileToUpload, final Path remotePath);

    boolean exist(final ServerEntry serverEntry);

    Collection<ServerEntry> getChanges() throws SynchronizationException;

    void download(final ServerEntry serverPath, final FileOutputStream outputStream) throws SynchronizationException;

    ServerEntry download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException;

    void delete(final File fileToUpload, final Path remotePath) throws SynchronizationException;

    ServerEntry uploadAsNew(final Path remotePath, final File fileToUpload, final InputStream inputStream) throws SynchronizationException;

    ServerEntry uploadAsUpdated(final Path remotePath, final File fileToUpload, final InputStream inputStream) throws SynchronizationException;

    List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException;

    List<ServerEntry> getRootFileList() throws SynchronizationException;

    void createFolder(final Path folder) throws SynchronizationException;

    AccountQuota getQuota() throws SynchronizationException;
}
