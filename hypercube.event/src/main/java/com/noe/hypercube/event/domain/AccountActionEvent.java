package com.noe.hypercube.event.domain;

public abstract class AccountActionEvent implements IEvent {

    private final String account;

    protected AccountActionEvent(final String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }
}
