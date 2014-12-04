package com.noe.hypercube.event.domain.request;


import com.noe.hypercube.event.domain.AccountActionEvent;

public class AccountConnectionRequest extends AccountActionEvent {

    public AccountConnectionRequest(final String account) {
        super(account);
    }
}
