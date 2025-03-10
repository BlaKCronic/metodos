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

public class GaussMethodController {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private TextField[][] matrixInputs;
    private TextField[] vectorInputs;
    private TextField[] resultFields;
    private Spinner<Integer> sizeSpinner;
    private int matrixSize = 3;

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Gauss - Análisis Numérico");

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.58);
        mainSplitPane.getStyleClass().add("main-container");

        VBox controlPanel = createControlPanel();
        VBox theoryPanel = createTheoryPanel();

        mainSplitPane.getItems().addAll(controlPanel, theoryPanel);

        Scene scene = new Scene(mainSplitPane, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("control-panel");
        panel.setPadding(new Insets(20));

        HBox sizeContainer = new HBox(10);
        Label sizeLabel = new Label("Tamaño de la matriz:");
        sizeSpinner = new Spinner<>(2, 10, 3);
        sizeSpinner.setEditable(true);
        sizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            matrixSize = newVal;
            updateMatrixInputs(panel);
        });
        
        Button exampleBtn = new Button("Cargar Ejemplo");
        exampleBtn.getStyleClass().add("example-btn");
        exampleBtn.setOnAction(e -> loadExample());
        
        sizeContainer.getChildren().addAll(sizeLabel, sizeSpinner, exampleBtn);

        VBox matrixContainer = new VBox(15);
        matrixContainer.getStyleClass().add("matrix-container");
        
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Resolver Sistema");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        buttonContainer.getChildren().addAll(btnCalculate, btnClear);

        VBox resultsContainer = new VBox(5);
        resultsContainer.getStyleClass().add("results-container");
        Label resultsLabel = new Label("Solución del Sistema:");
        resultsContainer.getChildren().add(resultsLabel);

        TableView<List<String>> stepsTable = createStepsTable();

        panel.getChildren().addAll(
            new Label("Configuración del Sistema:"),
            sizeContainer,
            new Separator(),
            matrixContainer,
            buttonContainer,
            new Label("Pasos de Eliminación:"),
            stepsTable,
            new Separator(),
            resultsContainer
        );

        updateMatrixInputs(panel);
        btnCalculate.setOnAction(e -> solveSystem(stepsTable, resultsContainer));

        return panel;
    }

    private void updateMatrixInputs(VBox panel) {
        panel.getChildren().stream()
            .filter(node -> node instanceof VBox && "matrix-container".equals(((VBox) node).getStyleClass().get(0)))
            .findFirst()
            .ifPresent(container -> panel.getChildren().remove(container));
        
        VBox matrixContainer = new VBox(15);
        matrixContainer.getStyleClass().add("matrix-container");
        
        GridPane matrixGrid = new GridPane();
        matrixGrid.setHgap(10);
        matrixGrid.setVgap(10);
        matrixGrid.setPadding(new Insets(10));
        
        matrixInputs = new TextField[matrixSize][matrixSize];
        vectorInputs = new TextField[matrixSize];
        
        for (int i = 0; i < matrixSize; i++) {
            Label colLabel = new Label("x" + (i + 1));
            matrixGrid.add(colLabel, i + 1, 0);
        }
        
        for (int i = 0; i < matrixSize; i++) {
            Label rowLabel = new Label("Ecuación " + (i + 1) + ":");
            matrixGrid.add(rowLabel, 0, i + 1);
            
            for (int j = 0; j < matrixSize; j++) {
                TextField field = new TextField("0");
                field.setPrefWidth(70);
                field.getStyleClass().add("matrix-field");
                matrixInputs[i][j] = field;
                matrixGrid.add(field, j + 1, i + 1);
            }
            
            Label equalLabel = new Label(" = ");
            matrixGrid.add(equalLabel, matrixSize + 1, i + 1);
            
            TextField bField = new TextField("0");
            bField.setPrefWidth(70);
            bField.getStyleClass().add("vector-field");
            vectorInputs[i] = bField;
            matrixGrid.add(bField, matrixSize + 2, i + 1);
        }
        
        matrixContainer.getChildren().addAll(
            new Label("Sistema de Ecuaciones:"),
            matrixGrid
        );
        
        int indexAfterSeparator = panel.getChildren().indexOf(
            panel.getChildren().stream()
                .filter(node -> node instanceof Separator)
                .findFirst()
                .orElse(null))
            + 1;
        
        panel.getChildren().add(indexAfterSeparator, matrixContainer);
        
        VBox resultsBox = (VBox) panel.getChildren().stream()
            .filter(node -> node instanceof VBox && "results-container".equals(((VBox) node).getStyleClass().get(0)))
            .findFirst()
            .orElse(new VBox());
            
        if (resultsBox.getChildren().size() > 1) {
            resultsBox.getChildren().subList(1, resultsBox.getChildren().size()).clear();
        }
        
        resultFields = new TextField[matrixSize];
    }

    private TableView<List<String>> createStepsTable() {
        TableView<List<String>> table = new TableView<>();
        
        TableColumn<List<String>, String> stepCol = new TableColumn<>("Paso");
        stepCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get(0)));
        stepCol.setPrefWidth(60);
        
        TableColumn<List<String>, String> descriptionCol = new TableColumn<>("Descripción");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get(1)));
        descriptionCol.setPrefWidth(350);
        
        TableColumn<List<String>, String> matrixCol = new TableColumn<>("Matriz Resultante");
        matrixCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get(2)));
        matrixCol.setPrefWidth(350);
        
        table.getColumns().addAll(stepCol, descriptionCol, matrixCol);
        table.setPrefHeight(250);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Fundamentos del Método de Gauss");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de Eliminación Gaussiana\n\n" +
            "El método de eliminación gaussiana es un algoritmo para resolver sistemas de ecuaciones lineales mediante operaciones elementales por filas para transformar la matriz aumentada en una forma escalonada.\n\n" +
            "Algoritmo general:\n" +
            "1. Escribir el sistema como una matriz aumentada [A|b]\n" +
            "2. Aplicar operaciones elementales por filas para obtener ceros debajo de la diagonal principal\n" +
            "3. Realizar sustitución hacia atrás para encontrar las soluciones\n\n" +
            "Operaciones elementales por filas:\n" +
            "• Intercambiar dos filas\n" +
            "• Multiplicar una fila por un escalar no nulo\n" +
            "• Sumar a una fila un múltiplo de otra\n\n" +
            "Ventajas:\n" +
            "• Método directo (no iterativo)\n" +
            "• Implementación computacional eficiente\n" +
            "• Base para métodos más sofisticados\n\n" +
            "Limitaciones:\n" +
            "• Problemas con ceros en la diagonal principal\n" +
            "• Acumulación de errores de redondeo en sistemas grandes\n" +
            "• No adecuado para matrices dispersas grandes\n\n" +
            "Variantes importantes:\n" +
            "• Gauss-Jordan: Obtiene ceros arriba y abajo de la diagonal\n" +
            "• Gauss con pivoteo parcial: Selecciona el pivote más grande\n" +
            "• Gauss con pivoteo completo: Busca el pivote más grande en toda la submatriz"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void solveSystem(TableView<List<String>> stepsTable, VBox resultsContainer) {
        try {
            if (resultsContainer.getChildren().size() > 1) {
                resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
            }
            
            double[][] matrix = new double[matrixSize][matrixSize + 1];
            
            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    matrix[i][j] = Double.parseDouble(matrixInputs[i][j].getText().trim());
                }
                matrix[i][matrixSize] = Double.parseDouble(vectorInputs[i].getText().trim());
            }
            
            double[] solution = solveWithGauss(matrix, stepsTable);
            
            // Contenedor horizontal para resultados
            HBox resultsBox = new HBox(20);
            resultsBox.setAlignment(Pos.CENTER_LEFT);
            resultsBox.setPadding(new Insets(10));
            
            for (int i = 0; i < solution.length; i++) {
                HBox varBox = new HBox(5);
                varBox.setAlignment(Pos.CENTER_LEFT);
                
                Label varLabel = new Label("x" + (i + 1) + " = ");
                TextField resultField = new TextField(df.format(solution[i]));
                resultField.setEditable(false);
                resultField.getStyleClass().add("result-field");
                resultField.setPrefWidth(100);
                
                varBox.getChildren().addAll(varLabel, resultField);
                resultsBox.getChildren().add(varBox);
                resultFields[i] = resultField;
            }
            
            resultsContainer.getChildren().add(resultsBox);
            
        } catch (Exception e) {
            showErrorDialog("Error al resolver", e.getMessage());
        }
    }
    
    private double[] solveWithGauss(double[][] matrix, TableView<List<String>> stepsTable) {
        int n = matrix.length;
        double[] solution = new double[n];
        
        ObservableList<List<String>> steps = FXCollections.observableArrayList();
        steps.add(createStepRow(0, "Matriz inicial", matrixToString(matrix)));
        
        for (int k = 0; k < n - 1; k++) {
            int maxRow = k;
            double maxVal = Math.abs(matrix[k][k]);
            
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(matrix[i][k]) > maxVal) {
                    maxVal = Math.abs(matrix[i][k]);
                    maxRow = i;
                }
            }
            
            if (maxRow != k) {
                double[] temp = matrix[k];
                matrix[k] = matrix[maxRow];
                matrix[maxRow] = temp;
                
                steps.add(createStepRow(steps.size(), 
                    "Intercambiar fila " + (k+1) + " con fila " + (maxRow+1) + " (pivoteo)", 
                    matrixToString(matrix)));
            }
            
            if (Math.abs(matrix[k][k]) < 1e-10) {
                throw new ArithmeticException("El sistema no tiene solución única (matriz singular)");
            }
            
            for (int i = k + 1; i < n; i++) {
                double factor = matrix[i][k] / matrix[k][k];
                
                for (int j = k; j <= n; j++) {
                    matrix[i][j] -= factor * matrix[k][j];
                }
                
                steps.add(createStepRow(steps.size(), 
                    "Fila " + (i+1) + " = Fila " + (i+1) + " - (" + df.format(factor) + " × Fila " + (k+1) + ")", 
                    matrixToString(matrix)));
            }
        }
        
        if (Math.abs(matrix[n-1][n-1]) < 1e-10) {
            throw new ArithmeticException("El sistema no tiene solución única (matriz singular)");
        }
        
        solution[n-1] = matrix[n-1][n] / matrix[n-1][n-1];
        
        for (int i = n - 2; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += matrix[i][j] * solution[j];
            }
            solution[i] = (matrix[i][n] - sum) / matrix[i][i];
        }
        
        steps.add(createStepRow(steps.size(), 
            "Sustitución hacia atrás para obtener las soluciones", 
            "Resultado:\n" + solutionToString(solution)));
        
        stepsTable.setItems(steps);
        
        return solution;
    }
    
    private List<String> createStepRow(int step, String description, String matrixStr) {
        List<String> row = new ArrayList<>();
        row.add(String.valueOf(step + 1));
        row.add(description);
        row.add(matrixStr);
        return row;
    }
    
    private String matrixToString(double[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            sb.append("[ ");
            for (int j = 0; j < matrix[i].length - 1; j++) {
                sb.append(df.format(matrix[i][j])).append("\t");
            }
            sb.append("| ").append(df.format(matrix[i][matrix[i].length - 1])).append(" ]");
            if (i < matrix.length - 1) sb.append("\n");
        }
        return sb.toString();
    }
    
    private String solutionToString(double[] solution) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < solution.length; i++) {
            sb.append("x").append(i + 1).append(" = ").append(df.format(solution[i]));
            if (i < solution.length - 1) sb.append("\n");
        }
        return sb.toString();
    }
    
    private void loadExample() {
        clearInputs();
        
        if (matrixSize != 3) {
            sizeSpinner.getValueFactory().setValue(3);
        }
        
        matrixInputs[0][0].setText("2");
        matrixInputs[0][1].setText("1");
        matrixInputs[0][2].setText("-1");
        vectorInputs[0].setText("8");
        
        matrixInputs[1][0].setText("-3");
        matrixInputs[1][1].setText("-1");
        matrixInputs[1][2].setText("2");
        vectorInputs[1].setText("-11");
        
        matrixInputs[2][0].setText("-2");
        matrixInputs[2][1].setText("1");
        matrixInputs[2][2].setText("2");
        vectorInputs[2].setText("-3");
    }
    
    private void clearInputs() {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixInputs[i][j].setText("0");
            }
            vectorInputs[i].setText("0");
        }
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}