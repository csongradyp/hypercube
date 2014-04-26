package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.nio.file.Path;

public interface IUploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> {

    Class<ACCOUNT_TYPE> getAccountType();

    Class<ENTITY_TYPE> getEntityType();

    void uploadNew(File file, Path remotePath) throws SynchronizationException;

    void uploadUpdated(File file, Path remotePath) throws SynchronizationException;

    void delete(File file, Path remotePath) throws SynchronizationException;
}
