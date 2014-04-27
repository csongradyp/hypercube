package com.noe.hypercube.synchronization.downstream;

import com.noe.hypercube.domain.ServerEntry;

public interface IDownloader<ACCOUNT_TYPE> extends Runnable {

    void download(ServerEntry entry);
}
