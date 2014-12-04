package com.noe.hypercube.ui.domain.account;

import javafx.beans.property.BooleanProperty;

public class AccountInfo {

    private final String name;
    private final BooleanProperty connected;
    private final BooleanProperty attached;

    public AccountInfo(final String name, final BooleanProperty attached, final BooleanProperty connected) {
        this.name = name;
        this.attached = attached;
        this.connected = connected;
    }

    public String getName() {
        return name;
    }

    public Boolean isActive() {
        return connected.get();
    }

    public boolean isAttached() {
        return attached.get();
    }

    public BooleanProperty attachedProperty() {
        return attached;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected.set(connected);
    }
}
