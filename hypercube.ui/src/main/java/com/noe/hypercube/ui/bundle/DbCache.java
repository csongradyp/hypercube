package com.noe.hypercube.ui.bundle;

public final class DbCache {

    private static DbCache instance = new DbCache();

    private DbCache() {
    }

    public static DbCache getInstance() {
        return instance;
    }
}
