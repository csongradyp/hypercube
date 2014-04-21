package com.noe.hypercube.synchronization;


import com.dropbox.core.DbxWriteMode;

public class DbxAction {

    private DbxWriteMode writeMode;
    private Action action;

    public DbxAction(Action action) {
        this.action = action;
        switch (action) {
            case ADDED:
                writeMode = DbxWriteMode.add();
                break;
            case CHANGED:
                writeMode = DbxWriteMode.force();
                break;
            case REMOVED:
                break;
        }
    }

    public DbxWriteMode getWriteMode() {
        return writeMode;
    }

    public Action getAction() {
        return action;
    }
}
