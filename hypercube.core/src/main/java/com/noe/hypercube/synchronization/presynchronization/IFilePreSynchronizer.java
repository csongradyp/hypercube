package com.noe.hypercube.synchronization.presynchronization;

import java.io.File;
import java.util.Collection;

public interface IFilePreSynchronizer {

    void run(Collection<File> files);

}