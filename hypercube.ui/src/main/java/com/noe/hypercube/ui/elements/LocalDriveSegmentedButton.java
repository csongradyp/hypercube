package com.noe.hypercube.ui.elements;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.StorageEvent;
import com.noe.hypercube.ui.factory.IconFactory;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import net.engio.mbassy.listener.Handler;
import org.controlsfx.control.SegmentedButton;

public class LocalDriveSegmentedButton extends SegmentedButton implements com.noe.hypercube.event.EventHandler<StorageEvent> {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);

    public LocalDriveSegmentedButton() {
        List<ToggleButton> drives = collectLocalDrives();
        getButtons().addAll(drives);
        active.addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                deselectButtons();
            }
        });
        EventBus.subscribeToStorageEvent(this);
    }

    private List<ToggleButton> collectLocalDrives() {
        List<ToggleButton> drives = new ArrayList<>(5);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path root : rootDirectories) {
            drives.add(createLocalStorageButton(root));
        }
        return drives;
    }

    private ToggleButton createLocalStorageButton(final Path root) {
        ToggleButton button = new ToggleButton(root.toString(), new ImageView(IconFactory.getStorageIcon(root)));
        button.setFocusTraversable(false);
        button.setId(root.toString());
        return button;
    }

    private void deselectButtons() {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        }
    }

    public void setOnAction(final EventHandler<ActionEvent> eventHandler) {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            button.setOnAction(event -> {
                active.set(true);
                button.setSelected(true);
                if (eventHandler != null) {
                    eventHandler.handle(event);
                }
            });
        }
    }

    public boolean isActive() {
        return active.get();
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    @Handler( rejectSubtypes = true)
    public void onEvent(final StorageEvent event) {
        Platform.runLater(() -> {
            if(event.isAttached()) {
                final ToggleButton removableDriveButton = createLocalStorageButton(event.getStorage());
                getButtons().add(removableDriveButton);
            } else {
                getButtons().removeIf(storageButton -> storageButton.getId().equals(event.getStorage().toString()));
            }
        });

    }
}
