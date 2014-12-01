package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.SynchronizationException;

public interface IUploader<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity> extends Runnable {

    Class<ACCOUNT_TYPE> getAccountType();

    Class<ENTITY_TYPE> getEntityType();

//    void uploadNew(File file, Path remotePath) throws SynchronizationException;

    void uploadUpdated(final UploadEntity uploadEntity) throws SynchronizationException;

    void uploadNew(final UploadEntity uploadEntity) throws SynchronizationException;

//    void uploadUpdated(File file, Path remotePath) throws SynchronizationException;

//    void delete(File file, Path remotePath) throws SynchronizationException;

    void delete(final UploadEntity uploadEntity) throws SynchronizationException;

    void stop();

    void restart();
}