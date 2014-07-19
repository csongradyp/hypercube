package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.tray.HypercubeTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class FileCommander extends Application {

    private HypercubeTrayIcon trayIcon;
    private ResourceBundle messageBundle;

    @Override
    public void start(final Stage stage) throws Exception {
        trayIcon = new HypercubeTrayIcon(stage);
        messageBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        Parent fileCommander = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"), messageBundle);
        Scene scene = new Scene(fileCommander, 800, 600);
        stage.setScene(scene);
        stage.setTitle("HyperCube - Cloud connected");
        stage.getIcons().add(ImageBundle.getImage("tray.default"));
        stage.setOnCloseRequest(t -> hide(stage));
        Platform.setImplicitExit(false);
    }

    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            stage.hide();
            trayIcon.hide(messageBundle);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
