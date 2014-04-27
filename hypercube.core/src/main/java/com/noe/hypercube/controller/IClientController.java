package com.noe.hypercube.controller;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.SynchronizationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;

public interface IClientController {

    boolean exist(Path remotePath, Class<? extends Account> accountType);

    boolean exist(ServerEntry serverEntry, Class<? extends Account> accountType);

    Collection<ServerEntry> getChanges(Class<? extends Account> accountType)  throws SynchronizationException;

    void download(ServerEntry serverPath, OutputStream outputStream, Class<? extends Account> accountType)  throws SynchronizationException;;

    void download(String serverPath, OutputStream outputStream, Class<? extends Account> accountType, Object... extraArgs)  throws SynchronizationException;;

    void delete(Path remotePath, Class<? extends Account> accountType) throws SynchronizationException;

    ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream, Class<? extends Account> accountType) throws SynchronizationException;

    ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream, Class<? extends Account> accountType) throws SynchronizationException;

}
