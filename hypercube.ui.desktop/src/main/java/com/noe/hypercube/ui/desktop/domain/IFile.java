package com.noe.hypercube.ui.desktop.domain;

import javafx.beans.property.BooleanProperty;

import java.nio.file.Path;

public interface IFile {

    boolean isLocal();

    boolean isStepBack();

    void setStepBack(boolean stepBack);

    boolean isDirectory();

    Path getPath();

    Path getParentFile();

    String getName();

    long size();

    long lastModified();

    BooleanProperty getSelectionProperty();

    void setSelected(boolean selected);

    boolean isSelected();

    boolean isRoot();
}
