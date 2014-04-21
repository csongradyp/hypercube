package com.noe.hypercube.mapping.collector;

import com.noe.hypercube.domain.MappingEntity;

import javax.inject.Named;
import java.nio.file.Path;

@Named
public class LocalDirectoryCollector extends DirectoryCollector {

    @Override
    protected Path getDirectoryToMatch(MappingEntity mapping) {
        return mapping.getRemoteDir();
    }
}
