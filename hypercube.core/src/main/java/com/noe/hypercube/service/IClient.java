package com.noe.hypercube.service;

import com.noe.hypercube.domain.*;
import com.noe.hypercube.synchronization.SynchronizationException;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface IClient<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_ENTITY extends MappingEntity> {

    /**
     * Returns the official name of the file hosting service.
     * E.g.: Dropbox
     */
    String getAccountName();

    /**
     * Returns the account specific <code>Class</code> which is annotated with @Entity.
     */
    Class<ENTITY_TYPE> getEntityType();

    Class<MAPPING_ENTITY> getMappingType();

    Class<ACCOUNT_TYPE> getAccountType();

    /**
     * Checks whether the file exists on the server or not.
     *
     * @return {@code true} if the file exists on the server in the given path in the {@link com.noe.hypercube.domain.ServerEntry} instance.
     */
    boolean exist(final ServerEntry serverEntry) throws SynchronizationException;

    /**
     * Checks whether the file exists on the server or not.
     *
     * @return {@code true} if the file exists on the server in the given path in the {@link com.noe.hypercube.domain.UploadEntity} instance.
     */
    boolean exist(final UploadEntity uploadEntity) throws SynchronizationException;

    Collection<ServerEntry> getChanges() throws SynchronizationException;

    void download(final ServerEntry serverPath, final FileOutputStream outputStream) throws SynchronizationException;

    ServerEntry download(String serverPath, FileOutputStream outputStream, Object... extraArgs) throws SynchronizationException;

    void delete(final Path remoteFilePath) throws SynchronizationException;

    void delete(final String remoteFileId) throws SynchronizationException;

    ServerEntry uploadAsNew(final UploadEntity uploadEntity) throws SynchronizationException;

    ServerEntry uploadAsUpdated(final UploadEntity uploadEntity) throws SynchronizationException;

    List<ServerEntry> getFileList(final Path remoteFolder) throws SynchronizationException;

    List<ServerEntry> getRootFileList() throws SynchronizationException;

    void createFolder(final Path folder) throws SynchronizationException;

    AccountQuota getQuota() throws SynchronizationException;

    Boolean isConnected();

    SimpleBooleanProperty connectedProperty();

    /**
     * Renames remote file. After method call original file will not exist, just with the new name.
     * @param remoteFile
     * @param newName
     * @return
     */
    FileEntity rename(FileEntity remoteFile, String newName) throws SynchronizationException;

    FileEntity rename(ServerEntry remoteFile, String newName) throws SynchronizationException;
}
