package com.noe.hypercube.domain;


import java.util.Date;

public interface FileEntityFactory<ACCOUNT_TYPE, ENTITY_TYPE> {

//    FileEntity createFileEntity(String localPath, String revision, Date date);

    Class<ACCOUNT_TYPE> getAccountType();

    Class<ENTITY_TYPE> getEntityType();

    FileEntity createFileEntity(String localPath, String remotePath, String revision, Date lastModified);
}
