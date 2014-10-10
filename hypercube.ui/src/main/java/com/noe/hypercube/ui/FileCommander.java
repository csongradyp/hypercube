package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.tray.HypercubeTrayIcon;
import impl.org.controlsfx.i18n.Localization;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static javafx.scene.layout.AnchorPane.*;

public class FileCommander extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(FileCommander.class);

    private HypercubeTrayIcon trayIcon;
    private ResourceBundle messageBundle;
    private Scene scene;

    @Override
    public void start(final Stage stage) throws Exception {
        trayIcon = new HypercubeTrayIcon(stage);
        final Locale defaultLocale = new Locale(ConfigurationBundle.getLanguage());
        Localization.setLocale(defaultLocale);
        Parent fileCommander = getParentNode(defaultLocale);
        scene = new Scene(fileCommander, 1024, 768);
        stage.setScene(scene);
        stage.setTitle("HyperCube - Cloud connected");
        stage.getIcons().add(ImageBundle.getImage("tray.default"));
        stage.setOnCloseRequest(t -> hide(stage));
        Platform.setImplicitExit(false);
        ConfigurationBundle.activeLanguageProperty().addListener((observable, oldValue, newValue) -> changLanguage());
    }

    private void changLanguage() {
        final Locale locale = new Locale(ConfigurationBundle.getLanguage());
        Locale.setDefault(Locale.Category.DISPLAY, locale);
        Localization.setLocale(locale);
        loadView(locale);
    }

    private void loadView(final Locale locale) {
        try {
            Parent fileCommander = getParentNode(locale);
            scene.setRoot(fileCommander);
            setTopAnchor(fileCommander, 0d);
            setBottomAnchor(fileCommander, 0d);
            setRightAnchor(fileCommander, 0d);
            setLeftAnchor(fileCommander, 0d);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Parent getParentNode(Locale locale) throws IOException {
        messageBundle = ResourceBundle.getBundle("internationalization/messages", locale);
        return (Parent) FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"), messageBundle);
    }


    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            stage.hide();
            trayIcon.hide(messageBundle);
        });
    }
}
