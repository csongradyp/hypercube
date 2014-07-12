package com.noe.hypercube.synchronization;


public enum Action {

    ADDED("Added"), REMOVED("Removed"), CHANGED("Changed");

    private final String action;

    private Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
