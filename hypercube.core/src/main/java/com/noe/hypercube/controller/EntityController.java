package com.noe.hypercube.controller;


import com.noe.hypercube.domain.FileEntity;

import java.util.Collection;

public interface EntityController {

    void save(FileEntity entity);

    void delete(FileEntity entity);

    boolean delete(String id, Class<? extends FileEntity> entityClass);

    FileEntity get(FileEntity entity);

    FileEntity get(String localPath, Class<? extends FileEntity> entityClass);

    Collection getAll(Class<? extends FileEntity> entityClass);
}
