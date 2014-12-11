package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import com.noe.hypercube.ui.dialog.AboutDialog;
import com.noe.hypercube.ui.dialog.BindManagerDialog;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.elements.StateInfoLabel;
import com.noe.hypercube.ui.tray.menu.list.FileListView;
import com.noe.hypercube.ui.tray.menu.list.TrayFileListItem;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.Icon;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
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
    private FileListView<TrayFileListItem> fileListView;
    @FXML
    private AccountSegmentedButton accounts;
    @FXML
    private Menu languages;
    private final ResourceBundle messageBundle;

    public TrayMenu(final Stage stage) {
        messageBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
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
        addHistoryChangeListenerToStorages();
    }

    private void addHistoryChangeListenerToStorages() {
        final ObservableList<ToggleButton> buttons = accounts.getButtons();
        for (ToggleButton button : buttons) {
            addHistoryChangeListener(button.getId());
        }
        accounts.setOnButtonAdded(this::addHistoryChangeListener);
    }

    private List<TrayFileListItem> createListItems(final ObservableList<FileEvent> fileEvents) {
        final List<TrayFileListItem> trayFileListItems = new ArrayList<>(fileEvents.size());
        trayFileListItems.addAll(fileEvents.stream().map(fileEvent -> new TrayFileListItem(fileEvent, messageBundle)).collect(Collectors.toList()));
        return trayFileListItems;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accounts.addButton("Local", new Icon(AwesomeIcon.HOME, "18", "", ""));
        accounts.setOnAction(e -> Platform.runLater(() -> {
            final ToggleButton accountButton = (ToggleButton) e.getSource();
            final ObservableList<FileEvent> fileEvents = HistoryBundle.getLastSyncedFiles().get(accountButton.getId());
            fileListView.clearAndSet(createListItems(fileEvents));
            accountButton.setSelected(true);
        }));
        accounts.getButtons().get(0).fire();
        fileListView.setLimit(6);
        selectActiveLanguage();
        settings.setOnMouseClicked(mouseEvent -> settings.getContextMenu().show(getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY()));
    }

    private void selectActiveLanguage() {
        final String current = ConfigurationBundle.getLanguageLongName();
        final ObservableList<MenuItem> languageMenuItems = languages.getItems();
        languageMenuItems.stream().filter(languageMenuItem -> languageMenuItem.getText().equals(current)).forEach(languageMenuItem ->
                ((CheckMenuItem) languageMenuItem).setSelected(true));
    }

    private void addHistoryChangeListener(final String account) {
        final ObservableList<FileEvent> fileEvents = HistoryBundle.getLastSyncedFiles().get(account);
        fileEvents.addListener((ListChangeListener<FileEvent>) change -> {
            while (change.next()) {
                final List<? extends FileEvent> addedSubList = change.getAddedSubList();
                addedSubList.stream().filter(fileEvent -> change.wasAdded() && getSelectedButton(accounts).getId().equals(account)).forEach(fileEvent ->
                        fileListView.add(new TrayFileListItem(fileEvent, messageBundle)));
            }
        });
    }

    public ToggleButton getSelectedButton(final SegmentedButton segmentedButton) {
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
    public void onPowerOff() {
        System.exit(0);
    }

    @FXML
    public void onManageBindings() {
        new BindManagerDialog().show();
    }
    @FXML
    public void onShowAbout() {
        new AboutDialog().show();
    }

}
