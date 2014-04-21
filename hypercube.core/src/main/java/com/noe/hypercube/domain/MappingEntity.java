package com.noe.hypercube.domain;

import java.nio.file.Path;

public interface MappingEntity extends IEntity<String> {

    Path getRemoteDir();

    Path getLocalDir();

    Filter getFilter();
}
