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

public class NewtonInterpolationController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TableView<List<String>> pointsTable;
    private TableView<List<String>> dividedDiffTable;
    private TextField xInterpolateField;
    private TextField resultField;
    private VBox resultsContainer;
    private List<TextField> xFields = new ArrayList<>();
    private List<TextField> yFields = new ArrayList<>();
    private int maxPoints = 6;  // Máximo número de puntos permitidos
    private VBox pointsContainer; // Store reference to the points container

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Interpolación por Diferencias Divididas de Newton - Análisis Numérico");

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
        pointsContainer = createPointsContainer();

        // Sección para ingresar el valor a interpolar
        HBox interpolateContainer = createInterpolateContainer();

        // Botones principales
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Calcular Interpolación");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        Button btnExample1 = new Button("Ejemplo Log10");
        btnExample1.getStyleClass().add("example-btn");
        btnExample1.setOnAction(e -> loadExample());
        
        Button btnExample2 = new Button("Ejemplo Ln");
        btnExample2.getStyleClass().add("example-btn");
        btnExample2.setOnAction(e -> loadExample2());
        
        Button btnAddPoint = new Button("+ Punto");
        btnAddPoint.getStyleClass().add("add-btn");
        btnAddPoint.setOnAction(e -> addPointField());

        buttonContainer.getChildren().addAll(btnCalculate, btnClear, btnExample1, btnExample2, btnAddPoint);

        // Crear la tabla para los puntos
        pointsTable = createPointsTable();
        
        // Crear la tabla para las diferencias divididas
        dividedDiffTable = createDividedDiffTable();
        
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
            new Label("Método de Interpolación por Diferencias Divididas de Newton:"),
            pointsContainer,
            new Separator(),
            interpolateContainer,
            buttonContainer,
            new Label("Puntos Ingresados:"),
            pointsTable,
            new Label("Tabla de Diferencias Divididas:"),
            dividedDiffTable,
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

        // Inicialmente creamos campos para 3 puntos
        for (int i = 0; i < 3; i++) {
            addPointFieldToContainer(container, i);
        }
        
        return container;
    }
    
    private void addPointField() {
        if (xFields.size() < maxPoints) {
            // Use the stored reference to pointsContainer instead of trying to find it in the scene graph
            addPointFieldToContainer(pointsContainer, xFields.size());
        } else {
            showWarningDialog("Límite alcanzado", "No se pueden agregar más de " + maxPoints + " puntos.");
        }
    }
    
    private void addPointFieldToContainer(VBox container, int index) {
        HBox pointContainer = new HBox(10);
        pointContainer.setAlignment(Pos.CENTER_LEFT);
        Label pLabel = new Label("Punto " + (index + 1) + ":");
        Label xLabel = new Label("x" + index + " =");
        TextField xField = new TextField();
        xField.setPrefWidth(100);
        Label yLabel = new Label("y" + index + " =");
        TextField yField = new TextField();
        yField.setPrefWidth(100);
        pointContainer.getChildren().addAll(pLabel, xLabel, xField, yLabel, yField);
        
        // Si es un punto nuevo, agregarlo al contenedor
        if (index >= xFields.size()) {
            container.getChildren().add(pointContainer);
            xFields.add(xField);
            yFields.add(yField);
        }
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
    
    private TableView<List<String>> createDividedDiffTable() {
        TableView<List<String>> table = new TableView<>();
        table.setPrefHeight(150);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Método de Interpolación por Diferencias Divididas de Newton");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de Interpolación por Diferencias Divididas de Newton\n\n" +
            "La interpolación mediante diferencias divididas de Newton es un método más avanzado que la interpolación lineal. Permite aproximar una función utilizando un polinomio de grado n que pasa por n+1 puntos dados.\n\n" +
            "Algoritmo:\n" +
            "Para n+1 puntos (x₀, y₀), (x₁, y₁), ..., (xₙ, yₙ), el polinomio interpolador de Newton es:\n\n" +
            "P(x) = b₀ + b₁(x-x₀) + b₂(x-x₀)(x-x₁) + ... + bₙ(x-x₀)(x-x₁)...(x-xₙ₋₁)\n\n" +
            "donde los coeficientes b₀, b₁, ..., bₙ se calculan mediante diferencias divididas:\n\n" +
            "b₀ = f(x₀)\n" +
            "b₁ = f[x₁,x₀] = (f(x₁) - f(x₀)) / (x₁ - x₀)\n" +
            "b₂ = f[x₂,x₁,x₀] = (f[x₂,x₁] - f[x₁,x₀]) / (x₂ - x₀)\n" +
            "...\n" +
            "bₙ = f[xₙ,xₙ₋₁,...,x₀] = (f[xₙ,xₙ₋₁,...,x₁] - f[xₙ₋₁,...,x₀]) / (xₙ - x₀)\n\n" +
            "El proceso se puede organizar en una tabla de diferencias divididas que facilita el cálculo sistemático.\n\n" +
            "Ventajas:\n" +
            "• Mayor precisión que la interpolación lineal\n" +
            "• Permite interpolar con varios puntos, no solo dos\n" +
            "• No requiere puntos equidistantes\n" +
            "• Es adecuado para funciones no lineales\n" +
            "• La forma del polinomio facilita agregar nuevos puntos sin recalcular todo\n\n" +
            "Limitaciones:\n" +
            "• Mayor complejidad computacional\n" +
            "• Con muchos puntos, pueden aparecer oscilaciones no deseadas (fenómeno de Runge)\n" +
            "• Para polinomios de grado alto, pueden surgir problemas de precisión numérica\n\n" +
            "Aplicaciones:\n" +
            "• Aproximación de funciones complejas\n" +
            "• Resolución numérica de ecuaciones diferenciales\n" +
            "• Integración numérica\n" +
            "• Ajuste de curvas en análisis de datos experimentales\n" +
            "• Modelado matemático en ingeniería y ciencias físicas"
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
            
            // Obtener puntos válidos
            int validPointsCount = 0;
            for (int i = 0; i < xFields.size(); i++) {
                if (!xFields.get(i).getText().trim().isEmpty() && !yFields.get(i).getText().trim().isEmpty()) {
                    validPointsCount++;
                }
            }
            
            if (validPointsCount < 2) {
                throw new IllegalArgumentException("Se necesitan al menos 2 puntos para interpolar");
            }
            
            // Arreglos para puntos válidos
            double[] xValues = new double[validPointsCount];
            double[] yValues = new double[validPointsCount];
            
            int index = 0;
            for (int i = 0; i < xFields.size(); i++) {
                if (!xFields.get(i).getText().trim().isEmpty() && !yFields.get(i).getText().trim().isEmpty()) {
                    xValues[index] = Double.parseDouble(xFields.get(i).getText().trim());
                    yValues[index] = Double.parseDouble(yFields.get(i).getText().trim());
                    index++;
                }
            }
            
            double x = Double.parseDouble(xInterpolateField.getText().trim());
            
            // Calcular tabla de diferencias divididas
            double[][] divDiffTable = calculateDividedDiffTable(xValues, yValues);
            
            // Calcular el resultado de la interpolación
            double result = newtonInterpolation(xValues, divDiffTable, x);
            
            // Mostrar resultado
            resultField.setText(df.format(result));
            
            // Actualizar la tabla de puntos
            updatePointsTable(xValues, yValues, x, result);
            
            // Actualizar la tabla de diferencias divididas
            updateDividedDiffTable(xValues, divDiffTable);
            
            // Generar y mostrar el polinomio de interpolación
            String polynomial = generatePolynomialExpression(xValues, divDiffTable);
            showPolynomial(polynomial);
            
        } catch (Exception e) {
            showErrorDialog("Error en el cálculo", e.getMessage());
        }
    }
    
    private String generatePolynomialExpression(double[] xValues, double[][] divDiffTable) {
        StringBuilder polynomial = new StringBuilder();
        
        // Agregar el término b0
        polynomial.append(df.format(divDiffTable[0][0]));
        
        // Agregar los términos restantes
        for (int i = 1; i < xValues.length; i++) {
            double coef = divDiffTable[0][i];
            
            // Si el coeficiente es cero, omitir este término
            if (Math.abs(coef) < 1e-10) continue;
            
            // Agregar el signo
            if (coef > 0) {
                polynomial.append(" + ");
            } else {
                polynomial.append(" - ");
                coef = Math.abs(coef);
            }
            
            // Agregar el coeficiente
            polynomial.append(df.format(coef));
            
            // Agregar cada factor (x - xⱼ)
            for (int j = 0; j < i; j++) {
                polynomial.append("(x - ").append(df.format(xValues[j])).append(")");
            }
        }
        
        return polynomial.toString();
    }
    
    private void showPolynomial(String polynomial) {
        // Asegurarse de que el contenedor de resultados existe
        if (!resultsContainer.getChildren().stream().anyMatch(node -> node instanceof Label && 
                ((Label) node).getText().equals("Polinomio de interpolación:"))) {
            
            Label polyLabel = new Label("Polinomio de interpolación:");
            TextArea polyArea = new TextArea(polynomial);
            polyArea.setEditable(false);
            polyArea.setWrapText(true);
            polyArea.setPrefHeight(60);
            polyArea.getStyleClass().add("polynomial-area");
            
            resultsContainer.getChildren().addAll(new Separator(), polyLabel, polyArea);
        } else {
            // Actualizar el área de texto existente
            for (int i = 0; i < resultsContainer.getChildren().size(); i++) {
                if (resultsContainer.getChildren().get(i) instanceof TextArea) {
                    ((TextArea) resultsContainer.getChildren().get(i)).setText(polynomial);
                    break;
                }
            }
        }
    }
    
    private double[][] calculateDividedDiffTable(double[] xValues, double[] yValues) {
        int n = xValues.length;
        double[][] table = new double[n][n];
        
        // Primera columna: valores de f(x)
        for (int i = 0; i < n; i++) {
            table[i][0] = yValues[i];
        }
        
        // Calcular las diferencias divididas
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                table[i][j] = (table[i + 1][j - 1] - table[i][j - 1]) / (xValues[i + j] - xValues[i]);
            }
        }
        
        return table;
    }
    
    private double newtonInterpolation(double[] xValues, double[][] divDiffTable, double x) {
        int n = xValues.length;
        double result = divDiffTable[0][0]; // b0
        double term = 1.0;
        
        for (int i = 1; i < n; i++) {
            term *= (x - xValues[i - 1]);
            result += divDiffTable[0][i] * term;
        }
        
        return result;
    }
    
    private void validateInputs() throws IllegalArgumentException {
        // Validar que haya al menos algunos puntos con valor
        boolean hasValidPoints = false;
        for (int i = 0; i < xFields.size(); i++) {
            if (!xFields.get(i).getText().trim().isEmpty() && !yFields.get(i).getText().trim().isEmpty()) {
                hasValidPoints = true;
                break;
            }
        }
        
        if (!hasValidPoints) {
            throw new IllegalArgumentException("Debe ingresar al menos un par de puntos (x,y)");
        }
        
        // Validar que el valor a interpolar esté presente
        if (xInterpolateField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un valor a interpolar");
        }
        
        // Validar que los valores sean numéricos
        try {
            for (int i = 0; i < xFields.size(); i++) {
                if (!xFields.get(i).getText().trim().isEmpty()) {
                    Double.parseDouble(xFields.get(i).getText().trim());
                }
                if (!yFields.get(i).getText().trim().isEmpty()) {
                    Double.parseDouble(yFields.get(i).getText().trim());
                }
            }
            Double.parseDouble(xInterpolateField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los valores ingresados deben ser numéricos");
        }
        
        // Validar que no haya valores de x repetidos
        for (int i = 0; i < xFields.size(); i++) {
            if (xFields.get(i).getText().trim().isEmpty()) continue;
            
            double xi = Double.parseDouble(xFields.get(i).getText().trim());
            for (int j = i + 1; j < xFields.size(); j++) {
                if (xFields.get(j).getText().trim().isEmpty()) continue;
                
                double xj = Double.parseDouble(xFields.get(j).getText().trim());
                if (Math.abs(xi - xj) < 1e-10) {
                    throw new IllegalArgumentException("Los valores de x no pueden repetirse");
                }
            }
        }
    }
    
    private void updatePointsTable(double[] xValues, double[] yValues, double x, double result) {
        ObservableList<List<String>> points = FXCollections.observableArrayList();
        
        for (int i = 0; i < xValues.length; i++) {
            List<String> point = new ArrayList<>();
            point.add("Punto " + (i + 1));
            point.add(df.format(xValues[i]));
            point.add(df.format(yValues[i]));
            points.add(point);
        }
        
        // Añadir el punto interpolado
        List<String> interpPoint = new ArrayList<>();
        interpPoint.add("Interpolado");
        interpPoint.add(df.format(x));
        interpPoint.add(df.format(result));
        points.add(interpPoint);
        
        pointsTable.setItems(points);
    }
    
    private void updateDividedDiffTable(double[] xValues, double[][] divDiffTable) {
        int n = xValues.length;
        
        // Crear columnas dinámicamente
        dividedDiffTable.getColumns().clear();
        dividedDiffTable.getItems().clear();
        
        TableColumn<List<String>, String> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        xColumn.setPrefWidth(80);
        dividedDiffTable.getColumns().add(xColumn);
        
        TableColumn<List<String>, String> fxColumn = new TableColumn<>("f(x)");
        fxColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        fxColumn.setPrefWidth(120);
        dividedDiffTable.getColumns().add(fxColumn);
        
        for (int j = 1; j < n; j++) {
            final int columnIndex = j + 1;
            TableColumn<List<String>, String> column = new TableColumn<>("f[" + j + "]");
            column.setCellValueFactory(data -> {
                if (columnIndex < data.getValue().size()) {
                    return new javafx.beans.property.SimpleStringProperty(data.getValue().get(columnIndex));
                } else {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });
            column.setPrefWidth(120);
            dividedDiffTable.getColumns().add(column);
        }
        
        // Llenar la tabla con los datos
        ObservableList<List<String>> tableData = FXCollections.observableArrayList();
        for (int i = 0; i < n; i++) {
            List<String> row = new ArrayList<>();
            row.add(df.format(xValues[i]));
            
            for (int j = 0; j < n; j++) {
                if (j <= n - i - 1) {
                    row.add(df.format(divDiffTable[i][j]));
                } else {
                    row.add("");
                }
            }
            
            tableData.add(row);
        }
        
        dividedDiffTable.setItems(tableData);
    }

    private void loadExample() {
        // Limpiar campos actuales
        clearInputs();
        
        // Asegurarse de que hay suficientes campos
        while (xFields.size() < 4) {
            addPointField();
        }
        
        // Ejemplo 1: Logaritmo base 10 tomado exactamente del PDF
        // Tabla del ejemplo del PDF:
        // Número | Logaritmo10
        // 3      | 0.477121
        // 3.5    | 0.544068
        // 4.5    | 0.653212
        // 5      | 0.698970
        // Calculando el valor del logaritmo 10 de 4
        xFields.get(0).setText("3");
        yFields.get(0).setText("0.477121");
        xFields.get(1).setText("3.5");
        yFields.get(1).setText("0.544068");
        xFields.get(2).setText("4.5");
        yFields.get(2).setText("0.653212");
        xFields.get(3).setText("5");
        yFields.get(3).setText("0.698970");
        xInterpolateField.setText("4");
        
        // Calcular automáticamente la interpolación
        calculateInterpolation();
    }
    
    private void loadExample2() {
        // Limpiar campos actuales
        clearInputs();
        
        // Asegurarse de que hay suficientes campos
        while (xFields.size() < 4) {
            addPointField();
        }
        
        // Ejemplo 2: Logaritmo natural del PDF (ejercicio propuesto)
        // Número | Ln
        // 1      | 0
        // 4      | 1.386294
        // 5      | 1.609438
        // 6      | 1.791760
        // Calculando el valor del ln de 2
        xFields.get(0).setText("1");
        yFields.get(0).setText("0");
        xFields.get(1).setText("4");
        yFields.get(1).setText("1.386294");
        xFields.get(2).setText("5");
        yFields.get(2).setText("1.609438");
        xFields.get(3).setText("6");
        yFields.get(3).setText("1.791760");
        xInterpolateField.setText("2");
        
        // Calcular automáticamente la interpolación
        calculateInterpolation();
    }

    private void clearInputs() {
        for (TextField field : xFields) {
            field.clear();
        }
        for (TextField field : yFields) {
            field.clear();
        }
        xInterpolateField.clear();
        resultField.clear();
        
        // Limpiar tablas
        pointsTable.getItems().clear();
        dividedDiffTable.getItems().clear();
        dividedDiffTable.getColumns().clear();
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