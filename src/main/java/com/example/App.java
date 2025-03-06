package com.example;

import com.example.controllers.SplashController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        new SplashController().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}