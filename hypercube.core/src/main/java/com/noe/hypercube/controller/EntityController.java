package com.noe.hypercube.controller;

import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.LocalFileEntity;

import java.util.Collection;

public interface EntityController {

    void save(FileEntity entity);

    void save(LocalFileEntity entity);

    void delete(FileEntity entity);

    void delete(String id, Class<? extends FileEntity> entityClass);

    FileEntity get(FileEntity entity);

    FileEntity get(String localPath, Class<? extends FileEntity> entityClass);

    Collection getAll(Class<? extends FileEntity> entityClass);
}
