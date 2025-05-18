package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LinearInterpolationController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TextField x0Field, y0Field, x1Field, y1Field, xInterpolateField;
    private TextField resultField;
    private TableView<List<String>> pointsTable;
    private VBox resultsContainer;

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Interpolación Lineal - Análisis Numérico");

        // Configuración principal del layout
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.58);
        mainSplitPane.getStyleClass().add("main-container");

        // Panel izquierdo: Controles y resultados
        VBox controlPanel = createControlPanel();
        
        // Panel derecho: Teoría y explicación
        VBox theoryPanel = createTheoryPanel();

        mainSplitPane.getItems().addAll(controlPanel, theoryPanel);

        // Configurar escena principal
        Scene scene = new Scene(mainSplitPane, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("control-panel");
        panel.setPadding(new Insets(20));

        // Sección para ingresar los puntos
        VBox pointsContainer = createPointsContainer();

        // Sección para ingresar el valor a interpolar
        HBox interpolateContainer = createInterpolateContainer();

        // Botones principales
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Calcular Interpolación");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        Button btnExample = new Button("Cargar Ejemplo");
        btnExample.getStyleClass().add("example-btn");
        btnExample.setOnAction(e -> loadExample());

        buttonContainer.getChildren().addAll(btnCalculate, btnClear, btnExample);

        // Crear la tabla para los puntos
        pointsTable = createPointsTable();
        
        // Contenedor para resultados
        resultsContainer = new VBox(10);
        resultsContainer.getStyleClass().add("results-container");
        Label resultsLabel = new Label("Resultado de la Interpolación:");
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.getStyleClass().add("result-field");
        resultField.setPrefWidth(300);
        
        HBox resultBox = new HBox(10);
        resultBox.setAlignment(Pos.CENTER_LEFT);
        resultBox.getChildren().addAll(new Label("f(x) = "), resultField);
        
        resultsContainer.getChildren().addAll(resultsLabel, resultBox);

        // Ensamblar panel
        panel.getChildren().addAll(
            new Label("Método de Interpolación Lineal:"),
            pointsContainer,
            new Separator(),
            interpolateContainer,
            buttonContainer,
            new Label("Puntos Ingresados:"),
            pointsTable,
            new Separator(),
            resultsContainer
        );

        // Manejador de eventos para el botón de calcular
        btnCalculate.setOnAction(e -> calculateInterpolation());

        return panel;
    }

    private VBox createPointsContainer() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(10));
        
        HBox point1Container = new HBox(10);
        point1Container.setAlignment(Pos.CENTER_LEFT);
        Label p1Label = new Label("Punto 1:");
        Label x0Label = new Label("x₀ =");
        x0Field = new TextField();
        x0Field.setPrefWidth(100);
        Label y0Label = new Label("y₀ =");
        y0Field = new TextField();
        y0Field.setPrefWidth(100);
        point1Container.getChildren().addAll(p1Label, x0Label, x0Field, y0Label, y0Field);
        
        HBox point2Container = new HBox(10);
        point2Container.setAlignment(Pos.CENTER_LEFT);
        Label p2Label = new Label("Punto 2:");
        Label x1Label = new Label("x₁ =");
        x1Field = new TextField();
        x1Field.setPrefWidth(100);
        Label y1Label = new Label("y₁ =");
        y1Field = new TextField();
        y1Field.setPrefWidth(100);
        point2Container.getChildren().addAll(p2Label, x1Label, x1Field, y1Label, y1Field);
        
        container.getChildren().addAll(point1Container, point2Container);
        return container;
    }

    private HBox createInterpolateContainer() {
        HBox container = new HBox(10);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER_LEFT);
        
        Label xLabel = new Label("Valor a Interpolar:");
        xInterpolateField = new TextField();
        xInterpolateField.setPrefWidth(150);
        xInterpolateField.setPromptText("Ingrese valor de x");
        
        container.getChildren().addAll(xLabel, xInterpolateField);
        return container;
    }

    private TableView<List<String>> createPointsTable() {
        TableView<List<String>> table = new TableView<>();
        
        String[] columns = {"Punto", "x", "y"};
        for (int i = 0; i < columns.length; i++) {
            final int columnIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(columns[i]);
            column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().get(columnIndex)
            ));
            
            if (columns[i].equals("Punto")) {
                column.setPrefWidth(80);
            } else {
                column.setPrefWidth(120);
            }
            table.getColumns().add(column);
        }
        
        table.setPrefHeight(150);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Método de Interpolación Lineal");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de Interpolación Lineal\n\n" +
            "La interpolación lineal es un método matemático utilizado para estimar valores desconocidos que se encuentran dentro del rango de un conjunto discreto de puntos de datos conocidos. Esta técnica supone que el cambio entre dos valores conocidos es lineal, lo que permite el cálculo de valores intermedios.\n\n" +
            "Algoritmo:\n" +
            "1) Dados dos puntos (x₀, y₀) y (x₁, y₁), para encontrar el valor de y correspondiente a un valor x que se encuentra entre x₀ y x₁, se utiliza la siguiente fórmula:\n\n" +
            "   f(x) = f(x₀) + [(f(x₁) - f(x₀)) / (x₁ - x₀)] * (x - x₀)\n\n" +
            "   donde:\n" +
            "   - f(x₀) es el valor de y₀\n" +
            "   - f(x₁) es el valor de y₁\n" +
            "   - x es el valor para el cual queremos interpolar\n" +
            "   - f(x) es el resultado de la interpolación\n\n" +
            "2) Este método asume una relación lineal entre los dos puntos, generando una línea recta que los conecta.\n\n" +
            "Ventajas:\n" +
            "• Sencillez de implementación y cálculo\n" +
            "• Eficiente para puntos que tienen una relación aproximadamente lineal\n" +
            "• No requiere muchos datos para realizar la estimación\n\n" +
            "Limitaciones:\n" +
            "• Solo es preciso si la relación entre los puntos es aproximadamente lineal\n" +
            "• No es adecuado para datos con comportamiento no lineal\n" +
            "• La precisión disminuye cuando los puntos están muy separados\n\n" +
            "Aplicaciones:\n" +
            "• Estimación de valores intermedios en tablas de datos\n" +
            "• Aproximación de funciones\n" +
            "• Análisis numérico\n" +
            "• Procesamiento de imágenes\n" +
            "• Gráficos por computadora"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void calculateInterpolation() {
        try {
            // Validar entradas
            validateInputs();
            
            // Obtener valores de los puntos
            double x0 = Double.parseDouble(x0Field.getText().trim());
            double y0 = Double.parseDouble(y0Field.getText().trim());
            double x1 = Double.parseDouble(x1Field.getText().trim());
            double y1 = Double.parseDouble(y1Field.getText().trim());
            double x = Double.parseDouble(xInterpolateField.getText().trim());
            
            // Validar que x esté entre x0 y x1
            if ((x < x0 && x < x1) || (x > x0 && x > x1)) {
                showWarningDialog("Advertencia", 
                    "El valor a interpolar está fuera del rango de los puntos dados.\n" +
                    "La interpolación lineal es más precisa dentro del rango de los puntos conocidos.\n" +
                    "En este caso, se está realizando una extrapolación.");
            }
            
            // Realizar la interpolación lineal
            double result = linearInterpolation(x0, y0, x1, y1, x);
            
            // Mostrar resultado
            resultField.setText(df.format(result));
            
            // Actualizar la tabla de puntos
            updatePointsTable(x0, y0, x1, y1);
            
        } catch (Exception e) {
            showErrorDialog("Error en el cálculo", e.getMessage());
        }
    }
    
    private double linearInterpolation(double x0, double y0, double x1, double y1, double x) {
        // Fórmula de interpolación lineal:
        // f(x) = f(x₀) + [(f(x₁) - f(x₀)) / (x₁ - x₀)] * (x - x₀)
        
        if (Math.abs(x1 - x0) < 1e-10) {
            throw new ArithmeticException("Los valores de x₀ y x₁ no pueden ser iguales");
        }
        
        return y0 + ((y1 - y0) / (x1 - x0)) * (x - x0);
    }
    
    private void validateInputs() throws IllegalArgumentException {
        if (x0Field.getText().trim().isEmpty() || y0Field.getText().trim().isEmpty() ||
            x1Field.getText().trim().isEmpty() || y1Field.getText().trim().isEmpty() ||
            xInterpolateField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }
        
        try {
            double x0 = Double.parseDouble(x0Field.getText().trim());
            double y0 = Double.parseDouble(y0Field.getText().trim());
            double x1 = Double.parseDouble(x1Field.getText().trim());
            double y1 = Double.parseDouble(y1Field.getText().trim());
            double x = Double.parseDouble(xInterpolateField.getText().trim());
            
            if (Math.abs(x1 - x0) < 1e-10) {
                throw new IllegalArgumentException("Los valores de x₀ y x₁ no pueden ser iguales");
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los valores numéricos son inválidos");
        }
    }
    
    private void updatePointsTable(double x0, double y0, double x1, double y1) {
        ObservableList<List<String>> points = FXCollections.observableArrayList();
        
        List<String> point1 = new ArrayList<>();
        point1.add("Punto 1");
        point1.add(df.format(x0));
        point1.add(df.format(y0));
        points.add(point1);
        
        List<String> point2 = new ArrayList<>();
        point2.add("Punto 2");
        point2.add(df.format(x1));
        point2.add(df.format(y1));
        points.add(point2);
        
        // Si hay un valor interpolado, añadirlo también
        if (!xInterpolateField.getText().trim().isEmpty() && !resultField.getText().trim().isEmpty()) {
            try {
                double x = Double.parseDouble(xInterpolateField.getText().trim());
                double y = Double.parseDouble(resultField.getText().trim());
                
                List<String> pointInterp = new ArrayList<>();
                pointInterp.add("Interpolado");
                pointInterp.add(df.format(x));
                pointInterp.add(df.format(y));
                points.add(pointInterp);
            } catch (NumberFormatException e) {
                // No hacer nada si los valores no son válidos
            }
        }
        
        pointsTable.setItems(points);
    }

    private void loadExample() {
        // Ejemplo del logaritmo base 10 de 4 interpolado entre 3 y 5
        x0Field.setText("3");
        y0Field.setText("0.477121");
        x1Field.setText("5");
        y1Field.setText("0.698970");
        xInterpolateField.setText("4");
        
        // Calcular automáticamente la interpolación
        calculateInterpolation();
    }

    private void clearInputs() {
        x0Field.clear();
        y0Field.clear();
        x1Field.clear();
        y1Field.clear();
        xInterpolateField.clear();
        resultField.clear();
        
        // Limpiar tabla
        pointsTable.getItems().clear();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarningDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}