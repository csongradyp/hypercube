package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.factory.IconFactory;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import org.controlsfx.control.SegmentedButton;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LocalDriveSegmentedButton extends SegmentedButton {

    public LocalDriveSegmentedButton() {
        List<ToggleButton> drives = collectLocalDrives();
        getButtons().addAll(drives);
        getButtons().get(0).setSelected(true);
    }

    private List<ToggleButton> collectLocalDrives() {
        List<ToggleButton> drives = new ArrayList<>(5);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path root : rootDirectories) {
            drives.add(createLocalStorageButton(root));
        }
        return drives;
    }

    private ToggleButton createLocalStorageButton(Path root) {
        ToggleButton button = new ToggleButton(root.toString(), new ImageView(IconFactory.getStorageIcon(root)));
        button.setFocusTraversable(false);
        return button;
    }

    public void setOnAction(EventHandler<ActionEvent> eventHandler) {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            button.setOnAction(eventHandler);
        }
    }

    public void deselectButtons() {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        }
    }
}
