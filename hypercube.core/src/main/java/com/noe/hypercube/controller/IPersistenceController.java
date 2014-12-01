package com.noe.hypercube.controller;

import com.noe.hypercube.persistence.domain.IEntity;
import com.noe.hypercube.persistence.domain.LocalFileEntity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public interface IPersistenceController extends EntityController, IMappingPersistenceController {

    Set<Class<IEntity>> getEntitiesMapping(String id);

    Collection<String> getLocalMappings();

    LocalFileEntity getLocalFileEntity(Path localFilePath);
}
