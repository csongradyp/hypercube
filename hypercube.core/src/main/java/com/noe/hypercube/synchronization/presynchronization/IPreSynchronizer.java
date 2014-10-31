package com.noe.hypercube.synchronization.presynchronization;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public interface IPreSynchronizer extends Callable<Boolean> {

    Path getTargetFolder();
}
