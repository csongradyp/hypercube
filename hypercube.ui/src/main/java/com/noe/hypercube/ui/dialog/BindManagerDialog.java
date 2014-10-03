package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.elements.LocalDriveSegmentedButton;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.util.Callback;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class BindManagerDialog extends Dialog implements Initializable {

    @FXML
    private LocalDriveSegmentedButton localDrives;
    @FXML
    private AccountSegmentedButton remoteDrives;
    @FXML
    private ListView<String> sourceFolderList;
    @FXML
    private ListView<Map.Entry<String, String>> mappingFolderList;

    public BindManagerDialog() {
        super(null, "", false);
        ResourceBundle bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("bindManagerDialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        final Action addBindAction = createAddBindAction(bundle);
        getActions().addAll(addBindAction, ACTION_CLOSE);
    }

    private Action createAddBindAction(ResourceBundle bundle) {
        return new Action(bundle.getString("tooltip.mapping.add"), new Consumer<ActionEvent>() {
            @Override
            public void accept(ActionEvent actionEvent) {
                onAdd();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mappingFolderList.setCellFactory(new Callback<ListView<Map.Entry<String, String>>, ListCell<Map.Entry<String, String>>>() {
            @Override
            public ListCell<Map.Entry<String, String>> call(ListView<Map.Entry<String, String>> param) {
                return new ListCell<Map.Entry<String, String>>() {
                    @Override
                    protected void updateItem(Map.Entry<String, String> entry, boolean empty) {
                        super.updateItem(entry, empty);
                        if (entry != null) {
                            final String key = entry.getKey();
                            if (key.isEmpty()) {
                                setGraphic(ImageBundle.getImageView("thumb.folder"));
                            } else {
                                setGraphic(ImageBundle.getAccountImageView(key));
                            }
                            setText(entry.getValue());
                        }
                    }
                };
            }
        });
        sourceFolderList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                final Collection<Map.Entry<String, String>> mappedFolders = new HashSet<>();
                if (remoteDrives.isActive()) {
                    final Set<String> localFolders = PathBundle.getLocalFoldersByRemote(newValue);
                    for (String localFolder : localFolders) {
                        final Map.Entry<String, String> entry = new DefaultMapEntry<>("", localFolder);
                        mappedFolders.add(entry);
                    }
                } else {
                    final Map<String, String> allRemoteFolders = PathBundle.getAllRemoteFolders(newValue);
                    mappedFolders.addAll(allRemoteFolders.entrySet());
                }
//                mappingFolderList.setItems(FXCollections.observableArrayList(mappedFolders));
                mappingFolderList.getItems().clear();
                mappingFolderList.getItems().addAll(mappedFolders);
            }
        });
        localDrives.setOnAction(event -> {
            remoteDrives.deselectButtons();
            final ToggleButton driveButton = (ToggleButton) event.getSource();
            driveButton.setSelected(true);
            final String driveLabel = driveButton.getText();
            sourceFolderList.setItems(FXCollections.observableArrayList(PathBundle.getLocalFolders(driveLabel)));
            sourceFolderList.getSelectionModel().selectFirst();
        });
        remoteDrives.setOnAction(event -> {
            localDrives.deselectButtons();
            final ToggleButton driveButton = (ToggleButton) event.getSource();
            driveButton.setSelected(true);
            final String account = driveButton.getId();
            sourceFolderList.setItems(FXCollections.observableArrayList(PathBundle.getRemoteFolders(account)));
            sourceFolderList.getSelectionModel().selectFirst();
        });
    }

    public void onAdd() {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.show();
    }

    @FXML
    public void onRemove() {

    }

}
