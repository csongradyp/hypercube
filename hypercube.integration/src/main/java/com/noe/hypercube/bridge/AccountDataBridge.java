package com.noe.hypercube.bridge;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.ui.bundle.AccountBundle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Properties;

@Named
public class AccountDataBridge {

    @Inject
    private IAccountController accountController;
    @Inject
    private Properties accountProperties;

    @PostConstruct
    public void transferData() {
        final Collection<AccountBox> accountBoxes = accountController.getAllAttached();
        for (AccountBox accountBox : accountBoxes) {
            final IClient client = accountBox.getClient();
            AccountBundle.registerAccount(client.getAccountName(), client.attachedProperty(), client.connectedProperty());
        }
    }
}
