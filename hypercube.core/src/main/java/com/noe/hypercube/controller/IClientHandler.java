package com.noe.hypercube.controller;

import com.noe.hypercube.service.Account;
import com.noe.hypercube.service.IClient;

public interface IClientHandler {

    IClient getClient(Class<? extends Account> account);

}
