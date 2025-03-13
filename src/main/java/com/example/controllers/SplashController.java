package com.example.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashController {
    public void show() {
        try {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(50));
            root.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 20;");

            Text title = new Text("Métodos Numéricos");
            title.setFont(Font.font(24));

            Text members = new Text("Integrantes:\nPonce Gonzalez Christian\nCruz Cruz Alejandro\nGadiel Karim Rios Rojas\nMartinez Cortez Irving\nMalagon Ortiz Emilio");
            members.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Button startBtn = new Button("Iniciar");
            startBtn.setOnAction(e -> {
                new MainMenuController().show();
                stage.close();
            });

            // Animaciones
            startBtn.setOnMouseEntered(e -> animateButton(startBtn, 1.10));
            startBtn.setOnMouseExited(e -> animateButton(startBtn, 1.0));

            root.getChildren().addAll(title, members, startBtn);

            Scene scene = new Scene(root, 600, 400);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al iniciar la aplicación: " + e.getMessage()).show();
            System.exit(1);
        }
    }

    private void animateButton(Button btn, double scale) {
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(200), btn);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }
}