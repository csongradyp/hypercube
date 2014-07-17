package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.tray.HypercubeTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class FileCommander extends Application {

    private static final String TRAY_DEFAULT_IMAGE_KEY = "tray.default";
    private static final GraphicsDevice DEFAULT_SCREEN_DEVICE = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private static final Integer WIDTH = DEFAULT_SCREEN_DEVICE.getDisplayMode().getWidth();
    private static final Integer HEIGHT = DEFAULT_SCREEN_DEVICE.getDisplayMode().getHeight();

    private HypercubeTrayIcon trayIcon;

    @Override
    public void start(final Stage stage) throws Exception {

        trayIcon = new HypercubeTrayIcon(stage);
        Parent fileCommander = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        Scene scene = new Scene(fileCommander, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("HyperCube - Cloud connected");
        stage.getIcons().add(ImageBundle.getImage(TRAY_DEFAULT_IMAGE_KEY));
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
