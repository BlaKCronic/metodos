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

public class JacobiMethodController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TableView<List<String>> resultsTable;
    private VBox resultsContainer;
    private TextField[][] coefficientsFields;
    private TextField[] constantsFields;
    private TextField tolField;
    private TextField sizeField;
    private int systemSize = 3;
    
    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Jacobi - Análisis Numérico");

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

        // Tamaño del sistema
        HBox sizeBox = new HBox(10);
        sizeBox.setAlignment(Pos.CENTER_LEFT);
        Label sizeLabel = new Label("Tamaño del sistema:");
        sizeField = new TextField("3");
        sizeField.setPrefWidth(50);
        Button updateSizeBtn = new Button("Actualizar");
        updateSizeBtn.setOnAction(e -> updateSystemSize());
        sizeBox.getChildren().addAll(sizeLabel, sizeField, updateSizeBtn);

        // Sección para ingresar coeficientes
        VBox systemContainer = new VBox(12);
        systemContainer.setPadding(new Insets(10));
        systemContainer.getStyleClass().add("system-container");
        Label systemLabel = new Label("Sistema de Ecuaciones Lineales:");
        
        GridPane coefficientsGrid = createCoefficientsGrid();
        systemContainer.getChildren().addAll(systemLabel, coefficientsGrid);

        // Campo para la tolerancia
        HBox toleranceBox = new HBox(10);
        toleranceBox.setAlignment(Pos.CENTER_LEFT);
        Label tolLabel = new Label("Tolerancia:");
        tolField = new TextField("0.000001");
        tolField.setPrefWidth(150);
        toleranceBox.getChildren().addAll(tolLabel, tolField);

        // Botones principales
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Resolver Sistema");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        Button btnExample = new Button("Cargar Ejemplo");
        btnExample.getStyleClass().add("example-btn");
        btnExample.setOnAction(e -> loadExample());

        buttonContainer.getChildren().addAll(btnCalculate, btnClear, btnExample);

        // Tabla de resultados
        Label resultsTableLabel = new Label("Proceso Iterativo:");
        
        // Contenedor para la tabla con ScrollPane
        ScrollPane tableScrollPane = new ScrollPane();
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setPrefHeight(300);
        tableScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        resultsTable = createResultsTable();
        tableScrollPane.setContent(resultsTable);

        // Contenedor para los resultados finales
        resultsContainer = new VBox(10);
        resultsContainer.getStyleClass().add("results-container");
        Label resultsLabel = new Label("Solución del Sistema:");
        resultsContainer.getChildren().add(resultsLabel);

        // Ensamblar panel
        panel.getChildren().addAll(
            sizeBox,
            systemContainer,
            new Separator(),
            toleranceBox,
            buttonContainer,
            resultsTableLabel,
            tableScrollPane,
            new Separator(),
            resultsContainer
        );

        // Manejadores de eventos
        btnCalculate.setOnAction(e -> solveSystem());

        return panel;
    }

    private GridPane createCoefficientsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Inicializar arreglos para los campos de texto
        coefficientsFields = new TextField[systemSize][systemSize];
        constantsFields = new TextField[systemSize];
        
        // Crear campos para los coeficientes y términos constantes
        for (int i = 0; i < systemSize; i++) {
            for (int j = 0; j < systemSize; j++) {
                coefficientsFields[i][j] = new TextField("0");
                coefficientsFields[i][j].setPrefWidth(60);
                grid.add(coefficientsFields[i][j], j * 2, i);
                
                if (j < systemSize - 1) {
                    Label varLabel = new Label("x" + (j + 1) + " +");
                    grid.add(varLabel, j * 2 + 1, i);
                } else {
                    Label varLabel = new Label("x" + (j + 1) + " =");
                    grid.add(varLabel, j * 2 + 1, i);
                }
            }
            
            constantsFields[i] = new TextField("0");
            constantsFields[i].setPrefWidth(60);
            grid.add(constantsFields[i], systemSize * 2, i);
        }
        
        return grid;
    }

    private TableView<List<String>> createResultsTable() {
        TableView<List<String>> table = new TableView<>();
        table.setRowFactory(tv -> {
            TableRow<List<String>> row = new TableRow<>();
            row.setPrefHeight(40);  // Aumentar la altura de la fila
            return row;
        });
        
        // Crear la primera columna para el número de iteración
        TableColumn<List<String>, String> iterCol = new TableColumn<>("Iter");
        iterCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        iterCol.setPrefWidth(50);
        table.getColumns().add(iterCol);
        
        // Crear columnas para valores de x1, x2, etc.
        for (int i = 0; i < systemSize; i++) {
            final int idx = i + 1;
            TableColumn<List<String>, String> xCol = new TableColumn<>("x" + idx);
            xCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(idx)));
            xCol.setPrefWidth(100);
            table.getColumns().add(xCol);
        }
        
        // Columnas para los errores individuales de cada ecuación
        for (int i = 0; i < systemSize; i++) {
            final int idx = i + 1;
            TableColumn<List<String>, String> errorCol = new TableColumn<>("Error Ec." + idx);
            errorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().get(systemSize + idx)
            ));
            errorCol.setPrefWidth(100);
            table.getColumns().add(errorCol);
        }
        
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Método de Jacobi");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de Jacobi\n\n" +
            "El método de Jacobi es un algoritmo iterativo para resolver sistemas de ecuaciones lineales.\n\n" +
            "Para un sistema Ax = b, el método se puede expresar como:\n\n" +
            "xᵏ⁺¹ = D⁻¹(b - (L + U)xᵏ)\n\n" +
            "Donde:\n" +
            "• A = D - L - U\n" +
            "• D es la matriz diagonal de A\n" +
            "• L es la parte triangular inferior estricta de A\n" +
            "• U es la parte triangular superior estricta de A\n\n" +
            "Algoritmo:\n" +
            "1) Despejar cada variable xᵢ en términos de las demás variables.\n" +
            "2) Establecer valores iniciales para todas las variables (por defecto cero).\n" +
            "3) Usando los valores actuales, calcular nuevos valores para cada variable.\n" +
            "4) Calcular el error entre las aproximaciones sucesivas.\n" +
            "5) Repetir hasta que el error sea menor que la tolerancia especificada.\n\n" +
            "Ventajas:\n" +
            "• Fácil de implementar\n" +
            "• Cada nueva aproximación solo depende de los valores de la iteración anterior\n" +
            "• Se puede paralelizar fácilmente\n\n" +
            "Limitaciones:\n" +
            "• La convergencia no está garantizada para cualquier sistema\n" +
            "• Converge si la matriz es diagonalmente dominante\n" +
            "• Puede ser lento para sistemas grandes"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void updateSystemSize() {
        try {
            int newSize = Integer.parseInt(sizeField.getText().trim());
            if (newSize < 2 || newSize > 10) {
                showErrorDialog("Error", "El tamaño del sistema debe estar entre 2 y 10");
                return;
            }
            
            systemSize = newSize;
            
            // Recrear el panel de coeficientes
            VBox systemContainer = (VBox) ((VBox) ((SplitPane) ((Scene) sizeField.getScene()).getRoot()).getItems().get(0)).getChildren().get(1);
            systemContainer.getChildren().set(1, createCoefficientsGrid());
            
            // Recrear la tabla de resultados
            ScrollPane scrollPane = (ScrollPane) ((VBox) ((SplitPane) ((Scene) sizeField.getScene()).getRoot()).getItems().get(0)).getChildren().get(6);
            resultsTable = createResultsTable();
            scrollPane.setContent(resultsTable);
            
            // Limpiar resultados
            if (resultsContainer.getChildren().size() > 1) {
                resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
            }
            
        } catch (NumberFormatException e) {
            showErrorDialog("Error", "Ingrese un número válido para el tamaño del sistema");
        }
    }

    private void solveSystem() {
        try {
            // Limpiar resultados anteriores
            if (resultsContainer.getChildren().size() > 1) {
                resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
            }

            // Obtener los coeficientes y términos constantes
            double[][] A = new double[systemSize][systemSize];
            double[] b = new double[systemSize];
            double[] x0 = new double[systemSize]; // Valores iniciales todos en cero
            
            for (int i = 0; i < systemSize; i++) {
                for (int j = 0; j < systemSize; j++) {
                    A[i][j] = Double.parseDouble(coefficientsFields[i][j].getText().trim());
                }
                b[i] = Double.parseDouble(constantsFields[i].getText().trim());
                x0[i] = 0.0; // Valores iniciales fijos en cero
            }
            
            double tol = Double.parseDouble(tolField.getText().trim());

            // Verificar si la matriz es diagonalmente dominante
            if (!isDiagonallyDominant(A)) {
                showWarningDialog("Advertencia", "La matriz no es diagonalmente dominante. El método puede no converger.");
            }

            // Resolver el sistema
            double[] solution = solveWithJacobi(A, b, x0, tol);

            // Mostrar la solución
            HBox solutionBox = new HBox(20);
            solutionBox.setAlignment(Pos.CENTER_LEFT);
            solutionBox.setPadding(new Insets(10));

            for (int i = 0; i < systemSize; i++) {
                Label xLabel = new Label("x" + (i + 1) + " = ");
                TextField xResult = new TextField(df.format(solution[i]));
                xResult.setEditable(false);
                xResult.getStyleClass().add("result-field");
                xResult.setPrefWidth(150);
                solutionBox.getChildren().addAll(xLabel, xResult);
            }

            Label iterLabel = new Label("Iteraciones: ");
            TextField iterResult = new TextField(String.valueOf((int)solution[systemSize]));
            iterResult.setEditable(false);
            iterResult.getStyleClass().add("result-field");
            iterResult.setPrefWidth(80);

            solutionBox.getChildren().addAll(iterLabel, iterResult);
            resultsContainer.getChildren().add(solutionBox);

        } catch (Exception e) {
            showErrorDialog("Error al resolver", e.getMessage());
        }
    }

    private boolean isDiagonallyDominant(double[][] A) {
        for (int i = 0; i < A.length; i++) {
            double diag = Math.abs(A[i][i]);
            double sum = 0;
            for (int j = 0; j < A.length; j++) {
                if (i != j) {
                    sum += Math.abs(A[i][j]);
                }
            }
            if (diag <= sum) {
                return false;
            }
        }
        return true;
    }

    private double[] solveWithJacobi(double[][] A, double[] b, double[] x0, double tol) {
        ObservableList<List<String>> steps = FXCollections.observableArrayList();
        
        int n = A.length;
        double[] x = new double[n];      // Nueva aproximación
        double[] xPrev = x0.clone();     // Aproximación anterior
        double[] errors = new double[n]; // Error para cada ecuación
        boolean converged = false;
        int iter = 0;
        
        while (!converged && iter < 1000) { // Añadimos límite de iteraciones para evitar bucles infinitos
            // Calcular la nueva aproximación
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum += A[i][j] * xPrev[j];
                    }
                }
                
                if (Math.abs(A[i][i]) < 1e-10) {
                    throw new ArithmeticException("División por cero: Coeficiente diagonal nulo o muy pequeño");
                }
                
                x[i] = (b[i] - sum) / A[i][i];
            }
            
            // Calcular los errores para cada ecuación
            converged = true;
            for (int i = 0; i < n; i++) {
                errors[i] = Math.abs(x[i] - xPrev[i]);
                // Verificar si todos los errores son menores que la tolerancia
                if (errors[i] > tol) {
                    converged = false;
                }
            }
            
            // Registrar esta iteración
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(iter + 1));
            
            // Añadir los valores de x
            for (int i = 0; i < n; i++) {
                row.add(df.format(x[i]));
            }
            
            // Añadir los errores individuales
            for (int i = 0; i < n; i++) {
                row.add(df.format(errors[i]));
            }
            
            steps.add(row);
            
            // Actualizar xPrev para la siguiente iteración
            for (int i = 0; i < n; i++) {
                xPrev[i] = x[i];
            }
            
            iter++;
        }
        
        // Mostrar los resultados en la tabla
        resultsTable.setItems(steps);
        
        // Agregar el número de iteraciones al final del array de solución
        double[] result = new double[n + 1];
        System.arraycopy(x, 0, result, 0, n);
        result[n] = iter;
        
        return result;
    }

    private void loadExample() {
        // Cargar un ejemplo de sistema diagonalmente dominante
        if (systemSize != 3) {
            systemSize = 3;
            sizeField.setText("3");
            updateSystemSize();
        }
        
        coefficientsFields[0][0].setText("10");
        coefficientsFields[0][1].setText("2");
        coefficientsFields[0][2].setText("1");
        constantsFields[0].setText("7");
        
        coefficientsFields[1][0].setText("1");
        coefficientsFields[1][1].setText("5");
        coefficientsFields[1][2].setText("1");
        constantsFields[1].setText("-8");
        
        coefficientsFields[2][0].setText("2");
        coefficientsFields[2][1].setText("3");
        coefficientsFields[2][2].setText("10");
        constantsFields[2].setText("6");
        
        tolField.setText("0.000001");
    }

    private void clearInputs() {
        // Limpiar todos los campos
        for (int i = 0; i < systemSize; i++) {
            for (int j = 0; j < systemSize; j++) {
                coefficientsFields[i][j].setText("0");
            }
            constantsFields[i].setText("0");
        }
        
        tolField.setText("0.000001");
        
        // Limpiar tabla y resultados
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