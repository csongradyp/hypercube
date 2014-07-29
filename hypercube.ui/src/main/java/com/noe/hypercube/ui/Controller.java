package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static javafx.scene.layout.AnchorPane.*;

public class Controller implements Initializable {

    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private ChoiceBox<String> languages;
    @FXML
    private AnchorPane commander;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languages.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ConfigurationBundle.setLanguage(newValue);
            changLanguage();
        });
    }

    @FXML
    public void onClose() {
        Platform.exit();
    }

    public void changLanguage() {
        final Locale locale = new Locale(ConfigurationBundle.getLanguage());
        commander.getChildren().clear();
        loadView(locale);
    }

    public void loadView(Locale locale) {
        try {
            AnchorPane fileCommander = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"), ResourceBundle.getBundle("internationalization/messages", locale));
            commander.getChildren().add(fileCommander);
            setTopAnchor(fileCommander, 0d);
            setBottomAnchor(fileCommander, 0d);
            setRightAnchor(fileCommander, 0d);
            setLeftAnchor(fileCommander, 0d);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
