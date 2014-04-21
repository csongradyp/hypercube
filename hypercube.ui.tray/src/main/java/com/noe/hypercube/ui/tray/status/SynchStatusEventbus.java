package com.noe.hypercube.ui.tray.status;

import java.util.ArrayList;
import java.util.List;

public class SynchStatusEventbus {

    private static final List<SynchStatusSubscriber> SUBSCRIBERS = new ArrayList<>();
    private static final List<SynchStatus> COUNTERS = new ArrayList<>();

    private SynchStatusEventbus() {}

    public static void subscribe(SynchStatusSubscriber subscriber) {
        SUBSCRIBERS.add(subscriber);
    }

    public static void started() {
        for (SynchStatusSubscriber subscriber : SUBSCRIBERS) {
            subscriber.onSynchStart();
        }
    }

    public static void finished() {
        for (SynchStatusSubscriber subscriber : SUBSCRIBERS) {
            subscriber.onSynchFinished();
        }
    }
}
