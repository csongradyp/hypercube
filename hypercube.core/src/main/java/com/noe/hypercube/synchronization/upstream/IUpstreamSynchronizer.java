package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;

public interface IUpstreamSynchronizer {

    void submit(File file, Action action) throws SynchronizationException;
}
