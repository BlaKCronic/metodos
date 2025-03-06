package com.example.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenuController {
    public void show() {
        Stage stage = new Stage();
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15;");
        
        Button secantBtn = createMethodButton("Método de la Secante");
        secantBtn.setOnAction(e -> new SecantMethodController().show());
        
        // Agregar más botones para otros métodos
        
        root.getChildren().addAll(secantBtn);
        
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    private Button createMethodButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        return btn;
    }
}