package com.noe.hypercube.ui.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        Scene scene = new Scene(root, 800, 600);
//        setUserAgentStylesheet(STYLESHEET_CASPIAN);
//        scene.getStylesheets().add("style/win7.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("HyperCube - Cloud connected");
//        primaryStage.initStyle(StageStyle.);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}