package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.FailedView;
import com.noe.hypercube.ui.HistoryView;
import com.noe.hypercube.ui.SynchronizationQueueView;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SynchronizationViewDialog extends Dialog<Boolean> implements Initializable {

    @FXML
    private Label title;
    @FXML
    private AnchorPane content;
    @FXML
    private SegmentedButton changeViewButtons;

    private ResourceBundle resourceBundle;
    private SynchronizationQueueView synchronizationQueueView;
    private HistoryView historyView;
    private FailedView failedView;

    public SynchronizationViewDialog() {
        FXMLLoader fxmlLoader = new FXMLLoader(BindManagerDialog.class.getClassLoader().getResource("synchronizationViewDialog.fxml"), resourceBundle);
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        setResultConverter(param -> true);
        getDialogPane().autosize();
        synchronizationQueueView = new SynchronizationQueueView();
        historyView = new HistoryView();
        failedView = new FailedView();
        final ObservableList<ToggleButton> accountButtons = changeViewButtons.getButtons();
        if(!accountButtons.isEmpty()) {
            accountButtons.get(0).fire();
        }
    }

    @FXML
    public void onShowBacklog(final ActionEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        button.setSelected(true);
        title.setText(resourceBundle.getString("dialog.sync.backlog"));
        content.getChildren().clear();
        content.getChildren().add(synchronizationQueueView);
    }

    @FXML
    public void onShowHistory(final ActionEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        button.setSelected(true);
        title.setText(resourceBundle.getString("dialog.sync.history"));
        content.getChildren().clear();
        content.getChildren().add(historyView);
    }

    @FXML
    public void onShowFailed(final ActionEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        button.setSelected(true);
        title.setText(resourceBundle.getString("dialog.sync.failed"));
        content.getChildren().clear();
        content.getChildren().add(failedView);
    }
}
