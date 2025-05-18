package com.example.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenuController {
    public void show() {
        try {
            Stage stage = new Stage();

            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15;");

            Button secantBtn = createMethodButton("Método de la Secante");
            secantBtn.setOnAction(e -> new SecantMethodController().show());

            Button gaussBtn = createMethodButton("Método de Gauss");
            gaussBtn.setOnAction(e -> new GaussMethodController().show());

            Button biseBtn = createMethodButton("Método de Biseccion");
            biseBtn.setOnAction(e -> new BisectionMethodController().show());

            Button jacoBtn = createMethodButton("Método de Jacobi");
            jacoBtn.setOnAction(e -> new JacobiMethodController().show());

            Button newRapMulBTN = createMethodButton("Metodo de Newton Rapson Multivariable");
            newRapMulBTN.setOnAction(e -> new NewtonRapsonMultiController().show());

            Button reMulBTN = createMethodButton("Metodo de regresion multiple");
            reMulBTN.setOnAction(e -> new RegresionMultipleController().show());

            Button linarIn = createMethodButton("Metodo de interpolacion lineal");
            linarIn.setOnAction(e -> new LinearInterpolationController().show());

            Button NewInter = createMethodButton("Metodo de interpolacion divididas Newton");
            NewInter.setOnAction(e -> new NewtonInterpolationController().show());

            Button exitBtn = createMethodButton("Salir");
            exitBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar Salida");
                alert.setHeaderText("¿Está seguro de que desea salir?");
                alert.setContentText("Esta acción no se puede deshacer.");
                alert.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().isDefaultButton()) {
                        stage.close();
                    }
                });
            });

            // Agregar más botones para otros métodos
            root.getChildren().addAll(secantBtn, gaussBtn, biseBtn, jacoBtn, newRapMulBTN, reMulBTN, linarIn, NewInter, exitBtn);

            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showFatalError("Error al cargar el menú principal", e);
        }
    }

    private Button createMethodButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        return btn;
    }

    private void showFatalError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Crítico");
        alert.setHeaderText(message);
        alert.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage());
        alert.showAndWait();
        System.exit(1);
    }
}