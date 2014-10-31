package com.noe.hypercube.event.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class MappingEvent implements IEvent {

    private final Path localFolder;
    private final Map<String, Path> remoteFolders;

    public MappingEvent(final Path localFolder) {
        this.localFolder = localFolder;
        remoteFolders = new HashMap<>();
    }

    public void addRemoteFolder(final String account, final Path remoteFolder) {
        remoteFolders.put(account, remoteFolder);
    }

    public Path getLocalFolder() {
        return localFolder;
    }

    public Map<String, Path> getRemoteFolders() {
        return remoteFolders;
    }

    @Override
    public String toString() {
        return "MappingEvent{" +
                "localFolder=" + localFolder +
                ", remoteFolders=" + remoteFolders +
                '}';
    }
}
