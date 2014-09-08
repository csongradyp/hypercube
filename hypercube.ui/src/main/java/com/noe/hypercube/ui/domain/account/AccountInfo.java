package com.noe.hypercube.ui.domain.account;

import javafx.beans.property.BooleanProperty;

public class AccountInfo {

    private final String name;
    private final BooleanProperty active;

    public AccountInfo(String name, BooleanProperty active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public Boolean isActive() {
        return active.get();
    }

    public void setActive(Boolean active) {
        this.active.set(active);
    }
}
