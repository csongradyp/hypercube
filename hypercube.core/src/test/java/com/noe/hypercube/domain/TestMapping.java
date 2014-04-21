package com.noe.hypercube.domain;

import java.nio.file.Path;

public class TestMapping implements MappingEntity {

    private Path localDir;
    private Path remoteDir;
    private Filter filters;

    public TestMapping(Path localdir, Path remoteDir) {
        this.localDir = localdir;
        this.remoteDir = remoteDir;
        filters = new TestFilter();
    }

    @Override
    public Path getRemoteDir() {
            return remoteDir;
    }

    @Override
    public Path getLocalDir() {
            return localDir;
    }

    @Override
    public Filter getFilter() {
        return filters;
    }

    @Override
    public String getId() {
        return localDir.toString();
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "localDir=" + localDir +
                ", remoteDir=" + remoteDir +
                ", fileFilters=" + filters +
                '}';
    }
}
