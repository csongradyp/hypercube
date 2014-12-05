package com.noe.hypercube.event.domain.response;


import com.noe.hypercube.event.domain.AccountConnectionEvent;

public class AccountConnectionResponse extends AccountConnectionEvent {

    private final Boolean attached;
    private final Boolean connected;

    public AccountConnectionResponse(final String account, final Boolean attached, final Boolean connected) {
        super(account);
        this.attached = attached;
        this.connected = connected;
    }

    public Boolean isAttached() {
        return attached;
    }

    public Boolean isConnected() {
        return connected;
    }
}
