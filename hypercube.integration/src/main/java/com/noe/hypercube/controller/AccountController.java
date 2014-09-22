package com.noe.hypercube.controller;


import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.mapping.IMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.Client;
import com.noe.hypercube.service.IClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
public class AccountController implements IAccountController {

    private Collection<Client> clients;
    private Collection<IMapper> mappers;
    private Collection<FileEntityFactory> entityFactories;
    @Inject
    private PersistenceController persistenceController;

    private final Map<Class<? extends Account>, AccountBox> accountBoxes;

    public AccountController() {
        accountBoxes = new LinkedHashMap<>();
    }

    public AccountController(final List<Client> clients, final List<IMapper> mappers, final List<FileEntityFactory> entityFactories) {
        this();
        this.clients = clients;
        this.mappers = mappers;
        this.entityFactories = entityFactories;
    }

    @PostConstruct
    private void createAccountBoxes() {
        Map<Class, Client> clientMap = clientsToMap(clients);
        Map<Class, IMapper> directoryMapperMap = mappersToMap(mappers);
        Map<Class, FileEntityFactory> entityFactoryMap = entityFactoriesToMap(entityFactories);
        persistenceController.createDaoMap();

        // TODO validate collections - size, classes, etc
        for (IClient currentClient : clients) {
            Class accountType = currentClient.getAccountType();
            Client client = clientMap.get(accountType);
            IMapper mapper = directoryMapperMap.get(accountType);
            FileEntityFactory entityFactory = entityFactoryMap.get(accountType);
            accountBoxes.put(accountType, new AccountBox(client, mapper, entityFactory, persistenceController));
        }
    }

    @Override
    public AccountBox getAccountBox(final Class<? extends Account> accountType) {
        return accountBoxes.get(accountType);
    }

    @Override
    public Collection<AccountBox> getAll() {
        return accountBoxes.values();
    }

    @Override
    public AccountBox getAccountBox(final String accountName) {
        for (Class<? extends Account> accountType : accountBoxes.keySet()) {
            if (accountType.getName().equals(accountName)) {
                return accountBoxes.get(accountType);
            }
        }
        throw new IllegalStateException(String.format("%s was not found in registered accounts", accountName));
    }

    private Map<Class, Client> clientsToMap(final Collection<Client> clients) {
        Map<Class, Client> map = new LinkedHashMap<>();
        for (Client client : clients) {
            map.put(client.getAccountType(), client);
        }
        return map;
    }

    private Map<Class, IMapper> mappersToMap(final Collection<IMapper> mappers) {
        Map<Class, IMapper> map = new LinkedHashMap<>();
        for (IMapper mapper : mappers) {
            map.put(mapper.getAccountType(), mapper);
        }
        return map;
    }

    private Map<Class, FileEntityFactory> entityFactoriesToMap(final Collection<FileEntityFactory> factories) {
        Map<Class, FileEntityFactory> map = new LinkedHashMap<>();
        for (FileEntityFactory entityFactory : factories) {
            map.put(entityFactory.getAccountType(), entityFactory);
        }
        return map;
    }

    public void setClients(Collection<Client> clients) {
        this.clients = clients;
    }

    public void setMappers(Collection<IMapper> mappers) {
        this.mappers = mappers;
    }

    public void setEntityFactories(Collection<FileEntityFactory> entityFactories) {
        this.entityFactories = entityFactories;
    }

}
