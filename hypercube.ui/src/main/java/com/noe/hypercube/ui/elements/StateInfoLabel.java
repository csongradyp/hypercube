package com.noe.hypercube.ui.elements;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.FileEventType;
import com.noe.hypercube.event.domain.type.SynchronizationSate;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.util.IconInjector;
import java.util.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import net.engio.mbassy.listener.Handler;

public class StateInfoLabel extends Label implements EventHandler<FileEvent> {

    private ResourceBundle messageBundle;
    private final Map<FileEventType, Collection<FileEvent>> events;

    public StateInfoLabel() {
        events = new HashMap<>();
        for (FileEventType fileEventType : FileEventType.values()) {
            events.put(fileEventType, new ArrayList<>());
        }
        messageBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        setFocusTraversable(false);
        final SynchronizationSate.State defaultState = SynchronizationSate.State.UP_TO_DATE;
        setText(messageBundle.getString(defaultState.getState()));
        IconInjector.setSyncStatusIcon(defaultState, this);
        EventBus.subscribeToFileEvent(this);
    }

    private void changeState(final FileEvent event) {
        final SynchronizationSate.State state = SynchronizationSate.getState(event);
        setText(messageBundle.getString(state.getState()));
        IconInjector.setSyncStatusIcon(state, this);
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        final FileEventType eventType = event.getEventType();
        if (FileEventType.STARTED == eventType) {
            events.get(eventType).add(event);
            Platform.runLater(() -> changeState(event));
        } else if (FileEventType.FINISHED == eventType) {
            final Collection<FileEvent> inProgress = events.get(eventType);
            inProgress.removeIf((inProgressEvent) -> inProgressEvent.getDirection() == event.getDirection()
                    && inProgressEvent.getLocalPath().equals(event.getLocalPath())
                    && inProgressEvent.getRemotePath().equals(event.getRemotePath()));
            if (inProgress.isEmpty()) {
                Platform.runLater(() -> changeState(event));
            }
        }
    }
}
