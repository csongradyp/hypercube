package com.noe.hypercube.ui.desktop;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private static Thread thread;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        Scene scene = new Scene(root, 1024, 768);
//        setUserAgentStylesheet(STYLESHEET_CASPIAN);
//        scene.getStylesheets().add("style/win7.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("HyperCube - Cloud connected");
//        primaryStage.initStyle(StageStyle.);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
