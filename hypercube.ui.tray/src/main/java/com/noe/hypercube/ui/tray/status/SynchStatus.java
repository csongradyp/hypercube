package com.noe.hypercube.ui.tray.status;

import java.util.concurrent.atomic.AtomicInteger;

public enum SynchStatus {

    DROPBOX, DRIVE;

    private AtomicInteger count = new AtomicInteger(0);

    public int getCount() {
        return count.get();
    }

    public void resetCount() {
        count.set(0);
    }

    public int incrementCount() {
        return count.incrementAndGet();
    }
}
