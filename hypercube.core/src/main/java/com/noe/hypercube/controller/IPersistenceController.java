package com.noe.hypercube.controller;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.IEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IPersistenceController extends EntityController, MappingController {

    Set<Class<IEntity>> getEntitiesMapping(String id);

    Collection<String> getLocalMappings();

    Map<String, List<FileEntity>> getMappings(String folder);
}
