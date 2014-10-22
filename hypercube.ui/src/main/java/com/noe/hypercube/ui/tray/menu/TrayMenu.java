package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import com.noe.hypercube.ui.elements.StateInfoLabel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class TrayMenu extends AnchorPane implements Initializable {

    @FXML
    private Button exit;
    @FXML
    private StateInfoLabel info;
    @FXML
    private Button show;
    @FXML
    private Button settings;
    @FXML
    private FileListView fileListView;
    @FXML
    private SegmentedButton accounts;
    @FXML
    private Menu languages;

    public TrayMenu(Stage stage) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("trayMenu.fxml"));
        fxmlLoader.setResources(messageBundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        show.setOnAction(actionEvent -> stage.show());
        addListenerForAccountChanges();
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                for (AccountInfo account : addedAccount) {
                    if (account.isActive()) {
                        accounts.getButtons().add(createAccountButton(account.getName()));
                        accounts.getButtons().get(0).fire();
                    }
                }
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    accounts.getButtons().removeIf(toggleButton -> toggleButton.getText().equals(account.getName()));
                    if (!accounts.getButtons().isEmpty()) {
                        accounts.getButtons().get(0).fire();
                    }
                }
            }
        });
    }

    private ToggleButton createAccountButton(final String account) {
        final ToggleButton accountButton = new ToggleButton();
        accountButton.setGraphic(ImageBundle.getAccountImageView(account));
        accountButton.setFocusTraversable(false);
        accountButton.setPrefHeight(accounts.getPrefHeight());
        accountButton.setOnAction(e -> {
            final ObservableList<FileEvent> fileEvents = HistoryBundle.getLastSyncedFiles().get(account);
            fileListView.clearAndSet(fileEvents);
            accountButton.setSelected(true);
        });
        addHistoryChangeListener(account);
        return accountButton;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileListView.setMessageBundle(resourceBundle);
        selectActiveLanguage();
        settings.setOnMouseClicked(mouseEvent -> settings.getContextMenu().show(getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY()));
    }

    private void selectActiveLanguage() {
        final String current = ConfigurationBundle.getLanguageLongName();
        final ObservableList<MenuItem> languageMenuItems = languages.getItems();
        for (MenuItem languageMenuItem : languageMenuItems) {
            if (languageMenuItem.getText().equals(current)) {
                ((CheckMenuItem) languageMenuItem).setSelected(true);
            }
        }
    }

    private void addHistoryChangeListener(final String account) {
        final ObservableList<FileEvent> fileEvents = HistoryBundle.getLastSyncedFiles().get(account);
        fileEvents.addListener((ListChangeListener<FileEvent>) change -> {
            while (change.next()) {
                final List<? extends FileEvent> addedSubList = change.getAddedSubList();
                for (FileEvent fileEvent : addedSubList) {
                    if (change.wasAdded() && getSelectedButton(accounts).getText().equals(account)) {
                        fileListView.add(fileEvent);
                    }
                }
            }
        });
    }

    public ToggleButton getSelectedButton(SegmentedButton segmentedButton) {
        final ObservableList<ToggleButton> buttons = segmentedButton.getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                return button;
            }
        }
        return buttons.get(0);
    }

    @FXML
    public void onLanguageChange(final ActionEvent event) {
        final MenuItem menuItem = (MenuItem) event.getSource();
        ConfigurationBundle.setLanguage(menuItem.getText());
    }

    @FXML
    public void onPowerOff(final ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void onManageBindings(final ActionEvent event) {
//        new BindManagerDialog().showAndWait().ifPresent(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                // send event
//                System.out.println(s);
//            }
//        });
    }

}
