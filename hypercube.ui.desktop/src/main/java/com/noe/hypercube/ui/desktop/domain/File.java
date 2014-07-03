package com.noe.hypercube.ui.desktop.domain;

import java.nio.file.Path;

public abstract class File implements IFile {

    private static final String PARENT_DIR_PLACEHOLDER = "[ . . ]";

    protected Path path;
    private boolean stepBack;
    private boolean selected;

    protected File( Path path ) {
        this.path = path;
    }

    @Override
    public boolean isStepBack() {
        return stepBack;
    }

    @Override
    public void setStepBack( boolean stepBack ) {
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
    public String getName() {
        if(stepBack) {
            return PARENT_DIR_PLACEHOLDER;
        }
        return path.getFileName().toString();
    }

    @Override
    public void setSelected( boolean selected ) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }
}
