package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.MappingRequest;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.elements.LocalDriveSegmentedButton;
import com.noe.hypercube.ui.factory.MappingListCellFactory;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BindManagerDialog extends Dialog<String> implements Initializable {

    @FXML
    private LocalDriveSegmentedButton localDrives;
    @FXML
    private AccountSegmentedButton remoteDrives;
    @FXML
    private ListView<String> sourceFolderList;
    @FXML
    private ListView<Map.Entry<String, String>> mappingFolderList;

    public BindManagerDialog() {
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
        setResultConverter(param -> {
            if (param.getButtonData() == ButtonBar.ButtonData.APPLY) {
                onAdd();
            }
            return null;
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final ButtonType buttonType = new ButtonType(resources.getString("tooltip.mapping.add"), ButtonBar.ButtonData.APPLY);
        getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CLOSE);
        mappingFolderList.setCellFactory(new MappingListCellFactory(remoteDrives));
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
                mappingFolderList.setItems(FXCollections.observableArrayList(mappedFolders));
            }
        });
        localDrives.setOnAction(onDriveAction(localRoot -> {
            remoteDrives.deselectButtons();
            return PathBundle.getLocalFolders(localRoot);
        }));
        remoteDrives.setOnAction(onDriveAction(account -> {
            localDrives.setActive(false);
            return PathBundle.getRemoteFolders(account);
        }));
        setEmptyTablePlaceholder(sourceFolderList, resources);
        setEmptyTablePlaceholder(mappingFolderList, resources);
        localDrives.getButtons().get(0).fire();
    }

    private EventHandler<ActionEvent> onDriveAction(final Callback<String, Set<String>> mappingListCallback) {
        return event -> {
            final ToggleButton driveButton = (ToggleButton) event.getSource();
            driveButton.setSelected(true);
            final String listRoot = driveButton.getId();
            final Set<String> folders = mappingListCallback.call(listRoot);
            sourceFolderList.setItems(FXCollections.observableArrayList(folders));
            if (folders.isEmpty()) {
                mappingFolderList.setItems(null);
            } else {
                sourceFolderList.getSelectionModel().selectFirst();
            }
            sourceFolderList.requestFocus();
        };
    }

    private void setEmptyTablePlaceholder(final ListView listView, final ResourceBundle resources) {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CHAIN_BROKEN, resources.getString("bind.empty"), "100px", "12", ContentDisplay.TOP);
        iconLabel.getGraphic().setOpacity(0.3d);
        listView.setPlaceholder(iconLabel);
    }

    public void onAdd() {
        final Optional<MappingRequest> requests = new AddMappingDialog().showAndWait();
        if (requests.isPresent()) {
            EventBus.publish(requests.get());
        }
    }
}
