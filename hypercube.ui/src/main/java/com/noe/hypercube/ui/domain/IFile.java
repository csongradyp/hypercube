package com.noe.hypercube.ui.domain;

import javafx.beans.property.BooleanProperty;

import java.nio.file.Path;

public interface IFile {

    boolean isLocal();

    boolean isStepBack();

    void setStepBack(boolean stepBack);

    boolean isDirectory();

    Path getPath();

    Path getParentFile();

    Path getParentDirectory();

    String getName();

    long size();

    long lastModified();

    BooleanProperty getMarkedProperty();

    void setMarked(boolean selected);

    boolean isMarked();

    void mark();

    boolean isRoot();
}
