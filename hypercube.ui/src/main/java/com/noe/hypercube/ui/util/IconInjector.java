package com.noe.hypercube.ui.util;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StateChangeEvent;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;

import static com.noe.hypercube.event.domain.StateChangeEvent.State.*;
import static com.noe.hypercube.event.domain.type.FileActionType.*;
import static com.noe.hypercube.event.domain.type.StreamDirection.DOWN;

public class IconInjector {

    public static final String ICON_SIZE = "20";

    public static void setFileStatusIcon(final FileEvent file, final Label label) {
        label.getStylesheets().add("style/status.css");
        if (ADDED == file.getActionType()) {
            AwesomeDude.setIcon(label, AwesomeIcon.INBOX, ICON_SIZE);
//            AwesomeDude.setIcon(label, AwesomeIcon.PLUS_SQUARE, ICON_SIZE);
            label.getGraphic().getStyleClass().add("added");
        } else if (DELETED == file.getActionType()) {
            AwesomeDude.setIcon(label, AwesomeIcon.TRASH_ALT, ICON_SIZE);
//            AwesomeDude.setIcon(label, AwesomeIcon.MINUS_SQUARE, ICON_SIZE);
            label.getGraphic().getStyleClass().add("deleted");
        } else if (UPDATED == file.getActionType()) {
            AwesomeDude.setIcon(label, AwesomeIcon.PENCIL, ICON_SIZE);
//            AwesomeDude.setIcon(label, AwesomeIcon.SHARE_SQUARE, ICON_SIZE);
//            AwesomeDude.setIcon( label, AwesomeIcon.PENCIL_SQUARE, ICON_SIZE );
            label.getGraphic().getStyleClass().add("updated");
        }
    }

    public static void setSyncStatusIcon(final StateChangeEvent.State state, Label label) {
        label.getGraphic().getStyleClass().clear();
        if (SYNCHRONIZING == state) {
            AwesomeDude.setIcon(label, AwesomeIcon.REFRESH);
            label.getGraphic().getStyleClass().add("synchronizing");
        } else if (UP_TO_DATE == state) {
            AwesomeDude.setIcon(label, AwesomeIcon.CHECK_CIRCLE);
            label.getGraphic().getStyleClass().add("up-to-date");
        } else if (OFFLINE == state) {
            AwesomeDude.setIcon(label, AwesomeIcon.WARNING);
            label.getGraphic().getStyleClass().add("offline");
        }
    }

    public static Label getStreamDirectionIcon(final FileEvent fileEvent) {
        final Label icon;
        if (fileEvent.getDirection() == DOWN) {
            icon = AwesomeDude.createIconLabel(AwesomeIcon.ARROW_CIRCLE_DOWN, ICON_SIZE);
        } else {
            icon = AwesomeDude.createIconLabel(AwesomeIcon.ARROW_CIRCLE_UP, ICON_SIZE);
        }
        icon.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.GREY, 7, 1, 1, 1));
        return icon;
    }
}
