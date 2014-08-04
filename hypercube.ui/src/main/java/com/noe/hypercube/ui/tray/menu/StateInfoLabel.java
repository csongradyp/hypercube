package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.StateChangeEvent;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Label;
import net.engio.mbassy.listener.Handler;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.StateChangeEvent.State.*;

public class StateInfoLabel extends Label implements EventHandler<StateChangeEvent> {

    private ResourceBundle messageBundle;

    public StateInfoLabel() {
        messageBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        setFocusTraversable(false);
        setText(messageBundle.getString("initializing"));
        AwesomeDude.setIcon(this, AwesomeIcon.SEARCH);
    }

    @Override
    @Handler
    public void onEvent(StateChangeEvent event) {
        final StateChangeEvent.State state = event.getState();
        setText(messageBundle.getString(state.getState()));
        if (SYNCHRONIZING == state) {
            AwesomeDude.setIcon(this, AwesomeIcon.REFRESH);
        } else if (UP_TO_DATE == state) {
            AwesomeDude.setIcon(this, AwesomeIcon.CHECK_CIRCLE);
        } else if (OFFLINE == state) {
            AwesomeDude.setIcon(this, AwesomeIcon.WARNING);
        }
    }
}
