package com.noe.hypercube.controller;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class ClientHandler implements IClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

    private final Map<Class<? extends Account>, IClient> clientMap;
    private final Collection<IClient<? extends Account, ? extends FileEntity>> clients;

    public ClientHandler(Collection<IClient<? extends Account, ? extends FileEntity>> clients) {
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
    public IClient getClient(Class<? extends Account> account) {
        IClient client = clientMap.get(account);
        if(client == null) {
            LOG.error("Failed to find client for {}", account.getName());
            throw new IllegalStateException("Failed to find client for " + account.getName());
        }
        return client;
    }
}
