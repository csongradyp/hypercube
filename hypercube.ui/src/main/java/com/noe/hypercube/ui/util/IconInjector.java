package com.noe.hypercube.ui.util;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.StateChangeEvent;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.AwesomeIconsStack;
import de.jensd.fx.fontawesome.Icon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;

import static com.noe.hypercube.event.domain.StateChangeEvent.State.*;
import static com.noe.hypercube.event.domain.type.FileActionType.*;
import static com.noe.hypercube.event.domain.type.StreamDirection.DOWN;

public class IconInjector {

    public static final String ICON_SIZE = "20";
    private static final String SECONDARY_ICON_SIZE = "12";

    public static void setFileStatusIcon(final FileEvent file, final Label label) {
        AwesomeIconsStack graphic = null;
        if (ADDED == file.getActionType()) {
            graphic = AwesomeIconsStack.create()
                    .add(new Icon(AwesomeIcon.FILE_ALT, ICON_SIZE, "", ""))
                    .add(new Icon(AwesomeIcon.PLUS, SECONDARY_ICON_SIZE, "-fx-text-fill: green", ""));
        } else if (DELETED == file.getActionType()) {
            graphic = AwesomeIconsStack.create()
                    .add(new Icon(AwesomeIcon.FILE_ALT, ICON_SIZE, "", ""))
                    .add(new Icon(AwesomeIcon.TRASH, SECONDARY_ICON_SIZE, "-fx-text-fill: red", ""));
        } else if (UPDATED == file.getActionType()) {
            graphic = AwesomeIconsStack.create()
                    .add(new Icon(AwesomeIcon.FILE_ALT, ICON_SIZE, "", ""))
                    .add(new Icon(AwesomeIcon.PENCIL, SECONDARY_ICON_SIZE, "-fx-text-fill: blue", ""));
        }
        if (graphic != null) {
            graphic.setAlignment(Pos.BOTTOM_LEFT);
            label.setGraphic(graphic);
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
