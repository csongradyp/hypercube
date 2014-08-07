package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StateChangeEvent;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Label;

import static com.noe.hypercube.event.domain.StateChangeEvent.State.OFFLINE;
import static com.noe.hypercube.event.domain.StateChangeEvent.State.SYNCHRONIZING;
import static com.noe.hypercube.event.domain.StateChangeEvent.State.UP_TO_DATE;

public class IconInjector {

    public static final String ICON_SIZE = "20";

    public static void setFileStatusIcon( final FileEvent file, final Label label ) {
        label.getStylesheets().add( "style/status.css" );
        switch ( file.getEventType() ){
        case NEW:
            AwesomeDude.setIcon( label, AwesomeIcon.PLUS_SQUARE, ICON_SIZE );
            label.getGraphic().getStyleClass().add( "added" );
            break;
        case DELETED:
            AwesomeDude.setIcon( label, AwesomeIcon.MINUS_SQUARE, ICON_SIZE );
            label.getGraphic().getStyleClass().add( "deleted" );
            break;
        case UPDATED:
            AwesomeDude.setIcon( label, AwesomeIcon.SHARE_SQUARE, ICON_SIZE );
//            AwesomeDude.setIcon( label, AwesomeIcon.PENCIL_SQUARE, ICON_SIZE );
            label.getGraphic().getStyleClass().add( "updated" );
            break;
        }
    }

    public static void setSyncStatusIcon( final StateChangeEvent.State state, Label label ) {
        label.getGraphic().getStyleClass().clear();
        if (SYNCHRONIZING == state) {
            AwesomeDude.setIcon( label, AwesomeIcon.REFRESH );
            label.getGraphic().getStyleClass().add( "synchronizing" );
        } else if (UP_TO_DATE == state) {
            AwesomeDude.setIcon(label, AwesomeIcon.CHECK_CIRCLE);
            label.getGraphic().getStyleClass().add( "up-to-date" );
        } else if (OFFLINE == state) {
            AwesomeDude.setIcon(label, AwesomeIcon.WARNING);
            label.getGraphic().getStyleClass().add("offline");
        }
    }
}
