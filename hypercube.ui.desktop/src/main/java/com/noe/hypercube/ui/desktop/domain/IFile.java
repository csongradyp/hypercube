package com.noe.hypercube.ui.desktop.domain;

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

    void setSelected(boolean selected);

    boolean isSelected();

    boolean isRoot();
}
