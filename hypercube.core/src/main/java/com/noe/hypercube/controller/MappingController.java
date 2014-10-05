package com.noe.hypercube.controller;


import com.noe.hypercube.domain.MappingEntity;

import java.nio.file.Path;
import java.util.Collection;

public interface MappingController {

    Collection<MappingEntity> getMappings(Class<? extends MappingEntity> mappingClass);

    Collection<MappingEntity> getAllMappings();

    void addMapping(MappingEntity mapping);

    void removeMapping(MappingEntity mapping);

    Path getRemoteFolder(Class<? extends MappingEntity> mappingType, Path targetFolder);
}
