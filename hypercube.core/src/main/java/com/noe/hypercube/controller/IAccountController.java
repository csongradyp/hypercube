package com.noe.hypercube.controller;

import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.service.Account;

import java.util.Collection;

public interface IAccountController {

    AccountBox getAccountBox(Class<? extends Account> accountType);

    Collection<AccountBox> getAll();

    AccountBox getAccountBox(String accountName);
}
