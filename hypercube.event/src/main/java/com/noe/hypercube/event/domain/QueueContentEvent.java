package com.noe.hypercube.event.domain;

import com.noe.hypercube.event.domain.type.QueueType;

public abstract class QueueContentEvent implements IEvent {

    private final QueueType type;
    private final String account;

    public QueueContentEvent(final QueueType type, final String account) {
        this.type = type;
        this.account = account;
    }

    public QueueType getType() {
        return type;
    }

    public String getAccount() {
        return account;
    }
}
