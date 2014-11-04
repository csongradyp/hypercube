package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.QueueContentEvent;
import com.noe.hypercube.event.domain.type.QueueType;

public class QueueContentRequest extends QueueContentEvent {

    public QueueContentRequest(final QueueType type, final String account) {
        super(type, account);
    }
}
