package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.nio.file.Path;

public interface IDownloader extends Runnable {

    void download(ServerEntry entry);

    void download(Path serverPath, Path localFolder) throws SynchronizationException;

    void stop();

    void restart();
}
