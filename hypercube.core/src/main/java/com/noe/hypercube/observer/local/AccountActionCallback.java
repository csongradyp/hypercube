package com.noe.hypercube.observer.local;

import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.nio.file.Path;

public interface AccountActionCallback {

    void call(final AccountBox accountBox, final Path remotePath) throws SynchronizationException;
}
