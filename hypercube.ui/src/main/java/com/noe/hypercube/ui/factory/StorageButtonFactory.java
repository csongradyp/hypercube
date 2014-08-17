package com.noe.hypercube.ui.factory;

import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.nio.file.Path;

public final class StorageButtonFactory {

    private StorageButtonFactory() {
    }

    public static ToggleButton create(final Path rootPath, EventHandler<MouseEvent> mouseEventEventHandler) {
        ToggleButton button = new ToggleButton(rootPath.toString(), new ImageView(IconFactory.getStorageIcon(rootPath)));
        button.setOnMouseClicked(mouseEventEventHandler);
        return button;
    }
}
