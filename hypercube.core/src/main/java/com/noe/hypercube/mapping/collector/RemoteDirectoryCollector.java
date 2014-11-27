package com.noe.hypercube.mapping.collector;

import com.noe.hypercube.persistence.domain.MappingEntity;

import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;

@Named
public class RemoteDirectoryCollector extends DirectoryCollector {

    @Override
    protected Path getDirectoryToMatch(MappingEntity mapping) {
        return Paths.get(mapping.getLocalDir());
    }
}
