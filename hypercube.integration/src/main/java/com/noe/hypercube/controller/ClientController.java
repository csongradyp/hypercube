package com.noe.hypercube.controller;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class ClientController implements IClientController {

    private static final Logger LOG = LoggerFactory.getLogger(ClientController.class);

    private final Map<Class<? extends Account>, IClient> clientMap;
    private final Collection<IClient<? extends Account, ? extends FileEntity>> clients;

    public ClientController(Collection<IClient<? extends Account, ? extends FileEntity>> clients) {
        this.clientMap = new LinkedHashMap<>();
        this.clients = clients;
    }

    @PostConstruct
    private void createClientMap() {
        for (IClient<? extends Account, ? extends FileEntity> client : clients) {
            clientMap.put(client.getAccountType(), client);
        }
    }

    @Override
    public boolean exist(Path remotePath, Class<? extends Account> account) {
        IClient client = clientMap.get(account);
        return client.exist(remotePath);
    }

    @Override
    public boolean exist(ServerEntry serverEntry, Class<? extends Account> account) {
        IClient client = clientMap.get(account);
        return client.exist(serverEntry);
    }

    @Override
    public Collection<ServerEntry> getChanges(Class<? extends Account> account) throws SynchronizationException {
        IClient client = clientMap.get(account);
        return client.getChanges();
    }

    @Override
    public void download(ServerEntry serverPath, OutputStream outputStream, Class<? extends Account> account) throws SynchronizationException {
        IClient client = clientMap.get(account);
        client.download(serverPath, outputStream);
    }

    @Override
    public void download(String serverPath, OutputStream outputStream, Class<? extends Account> account, Object... extraArgs) throws SynchronizationException {
        IClient client = clientMap.get(account);
        client.download(serverPath, outputStream);
    }

    @Override
    public void delete(Path remotePath, Class<? extends Account> account) throws SynchronizationException {
        IClient client = clientMap.get(account);
        client.delete(remotePath);
    }

    @Override
    public ServerEntry uploadAsNew(Path remotePath, File fileToUpload, InputStream inputStream, Class<? extends Account> account) throws SynchronizationException {
        return null;
    }

    @Override
    public ServerEntry uploadAsUpdated(Path remotePath, File fileToUpload, InputStream inputStream, Class<? extends Account> account) throws SynchronizationException {
        return null;
    }
}
