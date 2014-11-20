package com.noe.hypercube.event.domain.type;

import com.noe.hypercube.event.domain.FileEvent;

import static com.noe.hypercube.event.domain.type.FileEventType.FINISHED;
import static com.noe.hypercube.event.domain.type.FileEventType.STARTED;

public class SynchronizationSate {

    public enum State {
        UP_TO_DATE("uptodate"),
        SYNCHRONIZING("synchronizing"),
        OFFLINE("offline");

        private final String state;

        State(final String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }

    public static State getState(FileEvent event) {
        final FileEventType eventType = event.getEventType();
        if (eventType == STARTED) {
            return State.SYNCHRONIZING;
        }
        if (eventType == FINISHED) {
            return State.UP_TO_DATE;
        }
        return null;
    }
}

