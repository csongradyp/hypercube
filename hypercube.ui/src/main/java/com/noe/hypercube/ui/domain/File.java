package com.noe.hypercube.ui.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public abstract class File implements IFile {

    protected Path path;
    private boolean stepBack;
    private final SimpleBooleanProperty marked;
    private Collection<String> shared;

    protected File(Path path) {
        this.path = path;
        marked = new SimpleBooleanProperty(false);
        shared = new ArrayList<>();
    }

    @Override
    public boolean isStepBack() {
        return stepBack;
    }

    @Override
    public void setStepBack(boolean stepBack) {
        this.stepBack = stepBack;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public Path getParentFile() {
        return path.getParent();
    }

    @Override
    public Path getParentDirectory() {
        Path currentDir = path.getParent();
        if (!stepBack && currentDir != null) {
            if (isRootDirectory(currentDir)) {
                return currentDir;
            }
            return currentDir.getParent();
        }
        return path;
    }

    private boolean isRootDirectory(Path parentDir) {
        return parentDir.getParent() == null;
    }

    @Override
    public String getName() {
        if (stepBack) {
            return "";
        }
        return path.getFileName().toString();
    }

    @Override
    public BooleanProperty getMarkedProperty() {
        return marked;
    }

    @Override
    public void setMarked(boolean marked) {
        this.marked.set(marked);
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
    public String toString() {
        return path.toString();
    }

    @Override
    public boolean isShared() {
        return !shared.isEmpty();
    }

    @Override
    public Collection<String> sharedWith() {
        return shared;
    }

    public void setShared(Collection<String> accounts) {
        this.shared = accounts;
    }

    @Override
    public void share(String account) {
        shared.add(account);
    }
}
