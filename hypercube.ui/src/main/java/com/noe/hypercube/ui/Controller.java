package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.dialog.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;

public class Controller implements Initializable {

    @FXML
    private AnchorPane commander;
    @FXML
    private Button queue;
    @FXML
    private Menu languages;
    @FXML
    private FileManager fileManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final String current = ConfigurationBundle.getLanguageLongName();
        final ObservableList<MenuItem> languageMenuItems = languages.getItems();
        languageMenuItems.stream().filter(languageMenuItem -> languageMenuItem.getText().equals(current)).forEach(languageMenuItem ->
                ((CheckMenuItem) languageMenuItem).setSelected(true));
    }

    @FXML
    public void onClose() {
        Platform.exit();
    }
    @FXML
    public void onRefresh() {
        fileManager.getActiveFileView().refresh();
    }

    @FXML
    public void onShowSyncView() {
        final SynchronizationViewDialog synchronizationViewDialog = new SynchronizationViewDialog();
        synchronizationViewDialog.initOwner(commander.getScene().getWindow());
        synchronizationViewDialog.show();
    }

    @FXML
    public void onManageBindings() {
        new BindManagerDialog().show();
    }

    @FXML
    public void onLanguageChange(final ActionEvent event) {
        final MenuItem menuItem = (MenuItem) event.getSource();
        ConfigurationBundle.setLanguage(menuItem.getText());
    }

    @FXML
    public void onAddBindings() {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.initOwner(commander.getScene().getWindow());
        addMappingDialog.show();
    }

    public void onAddConnection() {
        final AddConnectionDialog addConnectionDialog = new AddConnectionDialog();
        addConnectionDialog.initOwner(commander.getScene().getWindow());
        addConnectionDialog.show();
    }

    public void onShowAbout() {
        new AboutDialog().show();
    }
}
