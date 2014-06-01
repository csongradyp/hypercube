package com.noe.hypercube.observer.remote;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Named
public class CloudObserverFactory {

    @Inject
    private IAccountController accountController;
    @Inject
    private IPersistenceController persistenceController;

    public List<CloudObserver> create() {
        LinkedList<CloudObserver> observers = new LinkedList<>();
        Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            observers.add(new CloudObserver(accountBox, persistenceController));
        }
        return observers;
    }
}
