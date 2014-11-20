package com.noe.hypercube.event.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class MappingEvent implements IEvent {

    public enum Action {
        ADD, REMOVE
    }

    private final Path localFolder;
    private final Map<String, Path> remoteFolders;
    private Action action;

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

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "MappingEvent{" +
                "localFolder=" + localFolder +
                ", remoteFolders=" + remoteFolders +
                '}';
    }
}
