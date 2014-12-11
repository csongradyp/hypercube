package com.noe.hypercube.bridge;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.service.IClient;
import com.noe.hypercube.ui.bundle.AccountBundle;
import java.util.Collection;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class AccountDataBridge {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDataBridge.class);

    @Inject
    private IAccountController accountController;
    @Inject
    private Properties accountProperties;

    @PostConstruct
    public void transferData() {
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            final IClient client = accountBox.getClient();
            LOG.debug("{} collected", client.getAccountName());
            AccountBundle.registerAccount(client.getAccountName(), client.attachedProperty(), client.connectedProperty());
        }
    }
}
