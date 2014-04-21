package com.noe.hypercube.ui.tray.status;

public interface SynchStatusSubscriber {

    void onSynchStart();
    void onSynchStateChanged();
    void onSynchFinished();
}
