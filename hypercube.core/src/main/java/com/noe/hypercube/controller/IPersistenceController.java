package com.noe.hypercube.controller;

import com.noe.hypercube.domain.IEntity;
import com.noe.hypercube.domain.LocalFileEntity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public interface IPersistenceController extends EntityController, MappingController {

    Set<Class<IEntity>> getEntitiesMapping(String id);

    Collection<String> getLocalMappings();

    LocalFileEntity getLocalFileEntity(Path localFilePath);
}
