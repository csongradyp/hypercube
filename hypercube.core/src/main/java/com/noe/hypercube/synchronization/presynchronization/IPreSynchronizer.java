package com.noe.hypercube.synchronization.presynchronization;

import java.nio.file.Path;

public interface IPreSynchronizer extends Runnable {

    Path getTargetFolder();
}
