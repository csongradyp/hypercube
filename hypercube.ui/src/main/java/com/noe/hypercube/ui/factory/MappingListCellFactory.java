package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.elements.MappingCrumbButton;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Map;

public class MappingListCellFactory implements Callback<ListView<Map.Entry<String, String>>, ListCell<Map.Entry<String, String>>> {

    private final AccountSegmentedButton remoteDrives;

    public MappingListCellFactory(AccountSegmentedButton remoteDrives) {
        this.remoteDrives = remoteDrives;
    }

    @Override
    public ListCell<Map.Entry<String, String>> call(ListView<Map.Entry<String, String>> param) {
        return new ListCell<Map.Entry<String, String>>() {
            @Override
            protected void updateItem(Map.Entry<String, String> entry, boolean empty) {
                super.updateItem(entry, empty);
                if(entry != null && !empty) {
                    setGraphic(createContent(entry));
                } else {
                    setGraphic(null);
                }
            }

            private AnchorPane createContent(final Map.Entry<String, String> entry) {
                final HBox folderBox = createFolderLabel(entry);
                final MappingCrumbButton removeMappingButton = createRemoveMappingButton();
                selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue) {
                       removeMappingButton.getStyleClass().add("label-selected");
                    } else {
                        removeMappingButton.getStyleClass().remove("label-selected");
                    }
                });
                removeMappingButton.addEventHandler(MouseEvent.MOUSE_CLICKED, getMouseEventEventHandler(entry));
                final HBox removeMappingBox = new HBox(removeMappingButton);
                removeMappingBox.setMaxWidth(30);
                AnchorPane pane = new AnchorPane(folderBox, removeMappingBox);
                AnchorPane.setLeftAnchor(folderBox, 0d);
                AnchorPane.setRightAnchor(removeMappingBox, 0d);
                return pane;
            }

            private MappingCrumbButton createRemoveMappingButton() {
                final MappingCrumbButton mappingButton = new MappingCrumbButton();
                mappingButton.setAdder(false);
                return mappingButton;
            }

            private HBox createFolderLabel(Map.Entry<String, String> entry) {
                final String key = entry.getKey();
                final Label folder = new Label();
                if (key.isEmpty()) {
                    folder.setGraphic(ImageBundle.getImageView("thumb.folder"));
                } else {
                    folder.setGraphic(ImageBundle.getAccountImageView(key));
                }
                folder.setText(entry.getValue());
                return new HBox(folder);
            }
        };
    }

    private EventHandler<MouseEvent> getMouseEventEventHandler(final Map.Entry<String, String> entry) {
        return event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                // send remove mapping request
                if (remoteDrives.isActive()) {
                    System.out.printf("remove local mapping %s from account %s%n", entry.getValue(), remoteDrives.getActiveAccount());
                } else {
                    System.out.printf("remove mapping %s from account %s%n", entry.getValue(), entry.getKey());
                }
            }
        };
    }
}
