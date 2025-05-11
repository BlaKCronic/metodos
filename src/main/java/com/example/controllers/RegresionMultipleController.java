package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RegresionMultipleController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TextField numVariablesField;
    private TextField numDataPointsField;
    private TableView<List<String>> dataTable;
    private TableView<List<String>> resultsTable;
    private VBox dataInputContainer;
    private VBox resultsContainer;
    private List<List<TextField>> dataFields;

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Regresión Lineal Múltiple - Análisis Numérico");

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

        VBox configContainer = createConfigContainer();

        dataInputContainer = new VBox(10);
        dataInputContainer.getStyleClass().add("data-input-container");
        
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Calcular Regresión");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        Button btnExample = new Button("Cargar Ejemplo");
        btnExample.getStyleClass().add("example-btn");
        btnExample.setOnAction(e -> loadExample());

        buttonContainer.getChildren().addAll(btnCalculate, btnClear, btnExample);

        resultsTable = createResultsTable();

        resultsContainer = new VBox(10);
        resultsContainer.getStyleClass().add("results-container");
        Label resultsLabel = new Label("Modelo de Regresión:");
        resultsContainer.getChildren().add(resultsLabel);

        panel.getChildren().addAll(
            new Label("Regresión Lineal Múltiple:"),
            configContainer,
            new Separator(),
            dataInputContainer,
            buttonContainer,
            new Label("Cálculos:"),
            resultsTable,
            new Separator(),
            resultsContainer
        );

        btnCalculate.setOnAction(e -> calculateRegression());

        return panel;
    }

    private VBox createConfigContainer() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(10));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        Label varLabel = new Label("Número de Variables Independientes (x):");
        numVariablesField = new TextField("2");
        numVariablesField.setPrefWidth(60);
        
        Label dataLabel = new Label("Número de Puntos de Datos:");
        numDataPointsField = new TextField("6");
        numDataPointsField.setPrefWidth(60);
        
        Button btnGenerateTable = new Button("Generar Tabla");
        btnGenerateTable.getStyleClass().add("generate-btn");
        btnGenerateTable.setOnAction(e -> generateDataTable());
        
        grid.addRow(0, varLabel, numVariablesField);
        grid.addRow(1, dataLabel, numDataPointsField);
        
        container.getChildren().addAll(grid, btnGenerateTable);
        return container;
    }

    private void generateDataTable() {
        try {
            int numVars = Integer.parseInt(numVariablesField.getText().trim());
            int numPoints = Integer.parseInt(numDataPointsField.getText().trim());
            
            if (numVars < 1 || numVars > 5) {
                showErrorDialog("Entrada inválida", "El número de variables debe estar entre 1 y 5");
                return;
            }
            
            if (numPoints < numVars + 1) {
                showErrorDialog("Entrada inválida", "El número de puntos debe ser al menos " + (numVars + 1));
                return;
            }
            
            dataInputContainer.getChildren().clear();
            
            GridPane dataGrid = new GridPane();
            dataGrid.setHgap(8);
            dataGrid.setVgap(8);
            dataGrid.setPadding(new Insets(10));
            
            dataGrid.add(new Label("y"), 0, 0);
            for (int i = 0; i < numVars; i++) {
                dataGrid.add(new Label("x" + (i+1)), i+1, 0);
            }
            
            dataFields = new ArrayList<>();
            
            for (int row = 0; row < numPoints; row++) {
                List<TextField> rowFields = new ArrayList<>();
                
                TextField yField = new TextField();
                yField.setPrefWidth(70);
                dataGrid.add(yField, 0, row+1);
                rowFields.add(yField);
                
                for (int col = 0; col < numVars; col++) {
                    TextField xField = new TextField();
                    xField.setPrefWidth(70);
                    dataGrid.add(xField, col+1, row+1);
                    rowFields.add(xField);
                }
                
                dataFields.add(rowFields);
            }
            
            ScrollPane scrollPane = new ScrollPane(dataGrid);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);
            
            dataInputContainer.getChildren().add(scrollPane);
            
        } catch (NumberFormatException e) {
            showErrorDialog("Entrada inválida", "Ingrese valores numéricos válidos");
        }
    }

    private TableView<List<String>> createResultsTable() {
        TableView<List<String>> table = new TableView<>();
        
        
        table.setPrefHeight(200);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Regresión Lineal Múltiple");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Regresión Lineal Múltiple\n\n" +
            "La regresión lineal múltiple es una extensión de la regresión lineal simple que incorpora múltiples variables independientes.\n\n" +
            "Ecuación General:\n" +
            "y = a₀ + a₁x₁ + a₂x₂ + ... + aₙxₙ\n\n" +
            "Procedimiento:\n" +
            "1. Formular el sistema de ecuaciones normales:\n\n" +
            "   Σy = na₀ + a₁Σx₁ + a₂Σx₂ + ... + aₙΣxₙ\n" +
            "   Σx₁y = a₀Σx₁ + a₁Σx₁² + a₂Σx₁x₂ + ... + aₙΣx₁xₙ\n" +
            "   Σx₂y = a₀Σx₂ + a₁Σx₁x₂ + a₂Σx₂² + ... + aₙΣx₂xₙ\n" +
            "   ...\n" +
            "   Σxₙy = a₀Σxₙ + a₁Σx₁xₙ + a₂Σx₂xₙ + ... + aₙΣxₙ²\n\n" +
            "2. Resolver el sistema para obtener los coeficientes a₀, a₁, a₂, ..., aₙ\n\n" +
            "3. Evaluar la calidad del ajuste mediante el coeficiente de correlación r:\n\n" +
            "   r = √[(St - Sr)/St]\n\n" +
            "   donde:\n" +
            "   St = Σ(yᵢ - ȳ)²\n" +
            "   Sr = Σ(yᵢ - ŷᵢ)²\n" +
            "   ŷᵢ = a₀ + a₁x₁ᵢ + a₂x₂ᵢ + ... + aₙxₙᵢ\n\n" +
            "4. Interpretación: Un valor de r cercano a 1 indica un buen ajuste del modelo a los datos.\n\n" +
            "Ventajas:\n" +
            "• Permite analizar la influencia de múltiples variables sobre la variable dependiente\n" +
            "• Modela relaciones complejas entre variables\n" +
            "• Permite predicciones más precisas\n\n" +
            "Limitaciones:\n" +
            "• Asume relaciones lineales entre variables\n" +
            "• Vulnerable a problemas de multicolinealidad\n" +
            "• Requiere un número adecuado de observaciones en relación al número de variables"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void calculateRegression() {
        try {
            if (resultsContainer.getChildren().size() > 1) {
                resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
            }
            
            if (dataFields == null || dataFields.isEmpty()) {
                showErrorDialog("Error", "Primero debe generar la tabla de datos");
                return;
            }
            
            int numVars = Integer.parseInt(numVariablesField.getText().trim());
            int numPoints = dataFields.size();
            
            double[] y = new double[numPoints];
            double[][] x = new double[numPoints][numVars];
            
            for (int i = 0; i < numPoints; i++) {
                List<TextField> row = dataFields.get(i);
                
                if (row.get(0).getText().trim().isEmpty()) {
                    showErrorDialog("Error", "Falta valor y en la fila " + (i+1));
                    return;
                }
                y[i] = Double.parseDouble(row.get(0).getText().trim());
                
                for (int j = 0; j < numVars; j++) {
                    if (row.get(j+1).getText().trim().isEmpty()) {
                        showErrorDialog("Error", "Falta valor x" + (j+1) + " en la fila " + (i+1));
                        return;
                    }
                    x[i][j] = Double.parseDouble(row.get(j+1).getText().trim());
                }
            }
            
            Pair<double[], Double> result = calculateMultipleRegression(y, x);
            double[] coefficients = result.getKey();
            double r = result.getValue();
            
            setupResultsTable(numVars);
            
            populateResultsTable(y, x, coefficients, r);
            
            showRegressionEquation(coefficients, r);
            
        } catch (Exception e) {
            showErrorDialog("Error al calcular", e.getMessage());
        }
    }
    
    private Pair<double[], Double> calculateMultipleRegression(double[] y, double[][] x) {
        int n = y.length;  
        int k = x[0].length;   
        
        double[][] matrixA = new double[k+1][k+1];
        double[] matrixB = new double[k+1];
        
        
        matrixA[0][0] = n;
        for (int j = 0; j < k; j++) {
            double sumX = 0;
            for (int i = 0; i < n; i++) {
                sumX += x[i][j];
            }
            matrixA[0][j+1] = sumX;
            matrixA[j+1][0] = sumX; 
        }
        
        double sumY = 0;
        for (int i = 0; i < n; i++) {
            sumY += y[i];
        }
        matrixB[0] = sumY;
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                double sumXiXj = 0;
                for (int p = 0; p < n; p++) {
                    sumXiXj += x[p][i] * x[p][j];
                }
                matrixA[i+1][j+1] = sumXiXj;
            }
            
            double sumXiY = 0;
            for (int p = 0; p < n; p++) {
                sumXiY += x[p][i] * y[p];
            }
            matrixB[i+1] = sumXiY;
        }
        
        double[] coefficients = solveSystem(matrixA, matrixB);
        
        double r = calculateCorrelationCoefficient(y, x, coefficients);
        
        return new Pair<>(coefficients, r);
    }
    
    private double[] solveSystem(double[][] A, double[] b) {
        int n = b.length;
        double[][] augMatrix = new double[n][n+1];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augMatrix[i][j] = A[i][j];
            }
            augMatrix[i][n] = b[i];
        }
        
        for (int k = 0; k < n; k++) {
            int maxRow = k;
            double maxVal = Math.abs(augMatrix[k][k]);
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(augMatrix[i][k]) > maxVal) {
                    maxVal = Math.abs(augMatrix[i][k]);
                    maxRow = i;
                }
            }
            
            if (maxRow != k) {
                for (int j = 0; j <= n; j++) {
                    double temp = augMatrix[k][j];
                    augMatrix[k][j] = augMatrix[maxRow][j];
                    augMatrix[maxRow][j] = temp;
                }
            }
            
            if (Math.abs(augMatrix[k][k]) < 1e-10) {
                throw new ArithmeticException("La matriz es singular o mal condicionada");
            }
            
            for (int i = k + 1; i < n; i++) {
                double factor = augMatrix[i][k] / augMatrix[k][k];
                for (int j = k; j <= n; j++) {
                    augMatrix[i][j] -= factor * augMatrix[k][j];
                }
            }
        }
        
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = augMatrix[i][n];
            for (int j = i + 1; j < n; j++) {
                x[i] -= augMatrix[i][j] * x[j];
            }
            x[i] /= augMatrix[i][i];
        }
        
        return x;
    }
    
    private double calculateCorrelationCoefficient(double[] y, double[][] x, double[] coefficients) {
        int n = y.length;
        
        double sumY = 0;
        for (double yi : y) {
            sumY += yi;
        }
        double yMean = sumY / n;
        
        double St = 0;
        for (double yi : y) {
            St += Math.pow(yi - yMean, 2);
        }
        
        double Sr = 0;
        for (int i = 0; i < n; i++) {
            double yPred = coefficients[0];
            for (int j = 0; j < x[0].length; j++) {
                yPred += coefficients[j+1] * x[i][j]; 
            }
            Sr += Math.pow(y[i] - yPred, 2);
        }
        
        double r = Math.sqrt((St - Sr) / St);
        return r;
    }
    
    private void setupResultsTable(int numVars) {
        resultsTable.getColumns().clear();
        resultsTable.getItems().clear();
        
        String[] columnNames = {"Estadística"};
        
        TableColumn<List<String>, String> statColumn = new TableColumn<>(columnNames[0]);
        statColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get(0)
        ));
        statColumn.setPrefWidth(150);
        resultsTable.getColumns().add(statColumn);
        
        TableColumn<List<String>, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get(1)
        ));
        valueColumn.setPrefWidth(150);
        resultsTable.getColumns().add(valueColumn);
    }
    
    private void populateResultsTable(double[] y, double[][] x, double[] coefficients, double r) {
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        
        for (int i = 0; i < coefficients.length; i++) {
            List<String> row = new ArrayList<>();
            if (i == 0) {
                row.add("Coeficiente a₀");
            } else {
                row.add("Coeficiente a" + i);
            }
            row.add(df.format(coefficients[i]));
            data.add(row);
        }
        
        List<String> rRow = new ArrayList<>();
        rRow.add("Coeficiente de correlación (r)");
        rRow.add(df.format(r));
        data.add(rRow);
        
        List<String> r2Row = new ArrayList<>();
        r2Row.add("Coeficiente de determinación (r²)");
        r2Row.add(df.format(r*r));
        data.add(r2Row);
        
        resultsTable.setItems(data);
    }
    
    private void showRegressionEquation(double[] coefficients, double r) {
        VBox equationBox = new VBox(10);
        equationBox.setAlignment(Pos.CENTER_LEFT);
        equationBox.setPadding(new Insets(15));
        equationBox.getStyleClass().add("equation-box");
        
        StringBuilder eqStr = new StringBuilder();
        eqStr.append("y = ");
        eqStr.append(df.format(coefficients[0]));
        
        for (int i = 1; i < coefficients.length; i++) {
            if (coefficients[i] >= 0) {
                eqStr.append(" + ");
            } else {
                eqStr.append(" - ");
            }
            eqStr.append(df.format(Math.abs(coefficients[i])));
            eqStr.append("x").append(i);
        }
        
        Label equationLabel = new Label(eqStr.toString());
        equationLabel.getStyleClass().add("equation-label");
        
        Label fitLabel = new Label("Coeficiente de correlación: r = " + df.format(r));
        fitLabel.getStyleClass().add("fit-label");
        
        String interpretation;
        if (r > 0.9) {
            interpretation = "Interpretación: Muy buen ajuste";
        } else if (r > 0.8) {
            interpretation = "Interpretación: Buen ajuste";
        } else if (r > 0.6) {
            interpretation = "Interpretación: Ajuste moderado";
        } else {
            interpretation = "Interpretación: Ajuste deficiente";
        }
        
        Label interpretationLabel = new Label(interpretation);
        interpretationLabel.getStyleClass().add("interpretation-label");
        
        equationBox.getChildren().addAll(equationLabel, fitLabel, interpretationLabel);
        
        resultsContainer.getChildren().add(equationBox);
    }

    private void loadExample() {
        numVariablesField.setText("2");
        numDataPointsField.setText("6");
        
        generateDataTable();
        
        double[][] exampleData = {
            {19, 0, 2},
            {12, 1, 2},
            {11, 2, 4},
            {24, 0, 4},
            {22, 1, 6},
            {15, 2, 6}
        };
        
        for (int i = 0; i < exampleData.length; i++) {
            dataFields.get(i).get(0).setText(String.valueOf(exampleData[i][0])); // y
            dataFields.get(i).get(1).setText(String.valueOf(exampleData[i][1])); // x1
            dataFields.get(i).get(2).setText(String.valueOf(exampleData[i][2])); // x2
        }
    }

    private void clearInputs() {
        if (dataFields != null) {
            for (List<TextField> row : dataFields) {
                for (TextField field : row) {
                    field.clear();
                }
            }
        }
        
        resultsTable.getItems().clear();
        
        if (resultsContainer.getChildren().size() > 1) {
            resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
        }
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