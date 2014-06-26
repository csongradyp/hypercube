package com.noe.hypercube.ui.desktop.observer;

import java.nio.file.Path;

public interface StorageEventTask {

    public void run(Path storage);
}
