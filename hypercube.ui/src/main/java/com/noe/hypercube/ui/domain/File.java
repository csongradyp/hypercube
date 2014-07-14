package com.noe.hypercube.ui.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.nio.file.Path;

public abstract class File implements IFile {

    private static final String PARENT_DIR_PLACEHOLDER = "[ . . ]";

    protected Path path;
    private boolean stepBack;
    private final SimpleBooleanProperty marked;

    protected File(Path path) {
        this.path = path;
        marked = new SimpleBooleanProperty(false);
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
        if ( !stepBack && currentDir != null) {
            if( isRootDirectory( currentDir ) ) {
                 return currentDir;
             }
             return currentDir.getParent();
         }
        return path;
    }

    private boolean isRootDirectory( Path parentDir ) {
        return parentDir.getParent() == null;
    }

    @Override
    public String getName() {
        if (stepBack) {
            return PARENT_DIR_PLACEHOLDER;
        }
        return path.getFileName().toString();
    }

    @Override
    public BooleanProperty getMarkedProperty() {
        return marked;
    }

    @Override
    public void setMarked( boolean marked ) {
        this.marked.set( marked );
    }

    @Override
    public boolean isMarked() {
        return marked.get();
    }

    @Override
    public void mark() {
        marked.set( !marked.get() );
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
