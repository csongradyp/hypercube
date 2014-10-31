package com.noe.hypercube.controller;


import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MappingController {

    Collection<MappingEntity> getMappings(Class<? extends MappingEntity> mappingClass);

    Collection<Path> getMappedRemotes(Class<? extends MappingEntity> mappingClass);

    Collection<MappingEntity> getAllMappings();

    void addMapping(MappingEntity mapping);

    void removeMapping(MappingEntity mapping);

    Collection<Path> getRemoteFolder(Class<? extends MappingEntity> mappingType, Path targetFolder);

    Map<String, List<FileEntity>> getMappedEntities(String folder);

    List<MappingEntity> getMappings(String folder);
}
