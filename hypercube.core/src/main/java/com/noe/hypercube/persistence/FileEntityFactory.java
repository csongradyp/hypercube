package com.noe.hypercube.persistence;


import com.noe.hypercube.persistence.domain.FileEntity;
import java.util.Date;

public interface FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> {

    FileEntity createFileEntity(final String localPath, final String remotePath, final String revision, final Date lastModified);

    Class<ACCOUNT_TYPE> getAccountType();

    Class<ENTITY_TYPE> getEntityType();
}
