package com.noe.hypercube.controller;


import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntityFactory;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class AccountController implements IAccountController {

    private Collection<IClient> clients;
    private Collection<DirectoryMapper> mappers;
    private Collection<FileEntityFactory> entityFactories;

    private final Map<Class<? extends Account>, AccountBox> accountBoxes;

    public AccountController(final Collection<IClient> clients, final Collection<DirectoryMapper> mappers, final Collection<FileEntityFactory> entityFactories) {
        this.clients = clients;
        this.mappers = mappers;
        this.entityFactories = entityFactories;
        accountBoxes = new LinkedHashMap<>();
    }

    @PostConstruct
    private void createAccountBoxes() {
        Map<Class, IClient> clientMap = toMap(clients);
        Map<Class, DirectoryMapper> directoryMapperMap = toMap2(mappers);
        Map<Class, FileEntityFactory> entityFactoryMap = toMap3(entityFactories);

        // TODO validate collections - size, classes, etc
        for (IClient currentClient : clients) {
            Class accountType = currentClient.getAccountType();
            IClient client = clientMap.get(accountType);
            DirectoryMapper mapper = directoryMapperMap.get(accountType);
            FileEntityFactory entityFactory = entityFactoryMap.get(accountType);
            accountBoxes.put(accountType, new AccountBox(client, mapper, entityFactory));
        }
    }

    @Override
    public AccountBox getAccountBox(Class<? extends Account> accountType) {
        return accountBoxes.get(accountType);
    }

    @Override
    public Collection<AccountBox> getAll() {
        return accountBoxes.values();
    }

    private Map<Class, IClient> toMap(Collection<IClient> clients) {
        Map<Class, IClient> map = new LinkedHashMap<>();
        for (IClient client : clients) {
            map.put(client.getAccountType(), client);
        }
        return map;
    }

    private Map<Class, DirectoryMapper> toMap2(Collection<DirectoryMapper> mappers) {
        Map<Class, DirectoryMapper> map = new LinkedHashMap<>();
        for (DirectoryMapper mapper : mappers) {
            map.put(mapper.getAccountType(), mapper);
        }
        return map;
    }

    private Map<Class, FileEntityFactory> toMap3(Collection<FileEntityFactory> factories) {
        Map<Class, FileEntityFactory> map = new LinkedHashMap<>();
        for (FileEntityFactory entityFactory : factories) {
            map.put(entityFactory.getAccountType(), entityFactory);
        }
        return map;
    }
}
