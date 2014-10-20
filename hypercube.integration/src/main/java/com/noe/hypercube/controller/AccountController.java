package com.noe.hypercube.controller;


import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.service.Account;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class AccountController implements IAccountController {

    @Inject
    private PersistenceController persistenceController;
    private Collection<AccountBox> accountBoxList;

    private final Map<Class<? extends Account>, AccountBox> accountBoxes;

    public AccountController() {
        accountBoxes = new LinkedHashMap<>();
    }

    public AccountController(final Collection<AccountBox> accountBoxList) {
        this();
        this.accountBoxList = accountBoxList;
    }

    @PostConstruct
    private void createAccountBoxes() {
        persistenceController.createDaoMap();
        for (AccountBox accountBox : accountBoxList) {
            accountBoxes.put(accountBox.getAccountType(), accountBox);
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
        for (AccountBox accountBox : accountBoxes.values()) {
            if(accountBox.getClient().getAccountName().equals(accountName)) {
                return accountBox;
            }
        }
        throw new IllegalStateException(String.format("%s was not found in registered accounts", accountName));
    }

}
