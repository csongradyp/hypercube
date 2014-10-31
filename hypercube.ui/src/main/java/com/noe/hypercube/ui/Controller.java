package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.dialog.AddMappingDialog;
import com.noe.hypercube.ui.dialog.BindManagerDialog;
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

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private AnchorPane commander;
    @FXML
    private Button queue;
    @FXML
    private Menu languages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final String current = ConfigurationBundle.getLanguageLongName();
        final ObservableList<MenuItem> languageMenuItems = languages.getItems();
        for (MenuItem languageMenuItem : languageMenuItems) {
            if(languageMenuItem.getText().equals(current)) {
              ((CheckMenuItem) languageMenuItem).setSelected(true);
            }
        }
    }

    @FXML
    public void onClose() {
        Platform.exit();
    }

    @FXML
    public void onShowSyncView() {
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
    public void onAddBindings(ActionEvent event) {
        new AddMappingDialog().show();
    }
}
