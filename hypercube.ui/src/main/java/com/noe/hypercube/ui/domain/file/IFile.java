package com.noe.hypercube.ui.domain.file;

import javafx.beans.property.BooleanProperty;

import java.nio.file.Path;
import java.util.Collection;

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

    BooleanProperty markedProperty();

    void setMarked(boolean selected);

    boolean isMarked();

    void mark();

    boolean isRoot();

    boolean isShared();

    boolean isSharedWith(String account);

    Collection<String> sharedWith();

    void share(String account);

    void share(Collection<String> account);

    String getOrigin();
}
