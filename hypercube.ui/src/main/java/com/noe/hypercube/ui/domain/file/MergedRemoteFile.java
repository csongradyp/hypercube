package com.noe.hypercube.ui.domain.file;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MergedRemoteFile implements IFile {

    private final boolean isDirectory;
    private final SimpleBooleanProperty marked;
    private Map<String, RemoteFile> files;

    public MergedRemoteFile(final Boolean isDirectory) {
        this.isDirectory = isDirectory;
        marked = new SimpleBooleanProperty(false);
        files = new HashMap<>();
    }


    public void merge(final RemoteFile remoteFile) {
        files.put(remoteFile.getOrigin(), remoteFile);
    }

    public RemoteFile getFile(final String account) {
        return files.get(account);
    }

    public Collection<RemoteFile> getFiles() {
        return files.values();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public long size() {
        return 0L;
    }

    @Override
    public long lastModified() {
        return 0L;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isShared() {
        return sharedWith().size() > 1;
    }

    @Override
    public String getOrigin() {
        return "Cloud";
    }

    @Override
    public boolean isStepBack() {
        return false;
    }

    @Override
    public void setStepBack(boolean stepBack) {
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public Path getParentFile() {
        return null;
    }

    @Override
    public Path getParentDirectory() {
        return null;
    }

    @Override
    public String getName() {
        return files.values().iterator().next().getName();
    }

    @Override
    public BooleanProperty markedProperty() {
        return marked;
    }

    @Override
    public void setMarked(boolean selected) {
        marked.set(selected);
    }

    @Override
    public boolean isMarked() {
        return marked.get();
    }

    @Override
    public void mark() {
        marked.set(!marked.get());
    }

    @Override
    public boolean isSharedWith(final String account) {
        return files.get(account) != null;
    }

    @Override
    public Collection<String> sharedWith() {
        return files.keySet();
    }

    @Override
    public void share(String account) {
        throw  new UnsupportedOperationException("Merged cloud file: Use share(RemoteFile remoteFile) method");
    }

    @Override
    public void share(Collection<String> account) {
        throw  new UnsupportedOperationException("Merged cloud file: Use share(RemoteFile remoteFile) method");
    }
}
