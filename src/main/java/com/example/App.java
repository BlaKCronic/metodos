package com.example;

import com.example.controllers.SplashController;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            new SplashController().show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error fatal en la aplicación: " + e.getMessage()).show();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}