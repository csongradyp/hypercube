package com.noe.hypercube.event.domain;

public abstract class AccountConnectionEvent extends AccountActionEvent {

    protected AccountConnectionEvent(final String account) {
        super(account);
    }
}
