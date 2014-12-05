package com.noe.hypercube.event.domain.request;


import com.noe.hypercube.event.domain.AccountConnectionEvent;

public class AccountConnectionRequest extends AccountConnectionEvent {

    public AccountConnectionRequest(final String account) {
        super(account);
    }
}
