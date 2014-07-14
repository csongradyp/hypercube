package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.tray.HypercubeTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FileCommander extends Application {

    private HypercubeTrayIcon trayIcon;

    @Override
    public void start(final Stage stage) throws Exception {
        trayIcon = new HypercubeTrayIcon(stage);
        Parent fileCommander = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
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
            trayIcon.hide();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
