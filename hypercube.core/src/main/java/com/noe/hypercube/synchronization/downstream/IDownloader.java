package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.domain.ServerEntry;

public interface IDownloader extends Runnable {

    void download(ServerEntry entry);

    void stop();

    void restart();
}
