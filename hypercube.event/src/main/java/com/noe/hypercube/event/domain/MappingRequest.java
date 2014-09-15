package com.noe.hypercube.event.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MappingRequest implements IEvent {

    private final Path localFolder;
    private final Map<String, Path> remotePaths;

    public MappingRequest(final Path localFolder) {
        this.localFolder = localFolder;
        remotePaths = new HashMap<>();
    }

    public void add(final String account, final Path path) {
        remotePaths.put(account, path);
    }

    public Path getLocalFolder() {
        return localFolder;
    }

    public Map<String, Path> getRemotePaths() {
        return remotePaths;
    }
}
