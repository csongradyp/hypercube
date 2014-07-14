package com.noe.hypercube.event.domain;

public class AccountEvent implements IEvent {

    private final Class accountClass;
    private final String accountName;

    public AccountEvent(final String accountName, final Class accountClass) {
        this.accountName = accountName;
        this.accountClass = accountClass;
    }

    public String getAccountName() {
        return accountName;
    }

    public Class getAccountType() {
        return accountClass;
    }
}
