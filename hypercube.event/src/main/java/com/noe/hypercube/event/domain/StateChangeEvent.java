package com.noe.hypercube.event.domain;

public class StateChangeEvent implements IEvent {

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

    private final State state;

    public StateChangeEvent(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public String getStateString() {
        return state.getState();
    }
}
