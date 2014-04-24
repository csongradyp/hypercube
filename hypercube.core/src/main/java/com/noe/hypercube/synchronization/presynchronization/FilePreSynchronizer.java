package com.noe.hypercube.synchronization.presynchronization;

import java.io.File;
import java.util.Collection;

public interface FilePreSynchronizer {

    void run(Collection<File> files);
}
