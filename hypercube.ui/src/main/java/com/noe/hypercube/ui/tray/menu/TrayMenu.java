package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
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
    private ListView<String> lastSynchList;
    @FXML
    private SegmentedButton accounts;

    private ResourceBundle messageBundle;

    public TrayMenu(Stage stage) {
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
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        show.setFocusTraversable(false);
        settings.setFocusTraversable(false);
        exit.setFocusTraversable(false);
        AwesomeDude.setIcon(show, AwesomeIcon.TABLE);
        AwesomeDude.setIcon(settings, AwesomeIcon.GEAR);
        AwesomeDude.setIcon(exit, AwesomeIcon.SIGN_OUT);
        exit.setOnAction(actionEvent -> System.exit(0));
    }

}
