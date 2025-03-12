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
import javafx.scene.chart.XYChart;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NewtonRapsonMulti {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TextField f1Field;
    private TextField f2Field;
    private TextField x0Field;
    private TextField y0Field;
    private TextField tolField;
    private TableView<List<String>> resultsTable;
    private VBox resultsContainer;

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Newton-Raphson Multivariable - Análisis Numérico");

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

        // Sección para ingresar las funciones
        VBox functionsContainer = createFunctionsContainer();

        // Campos de entrada para valores iniciales y tolerancia
        GridPane inputGrid = createInputGrid();

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
        resultsTable = createResultsTable();

        // Contenedor para los resultados finales
        resultsContainer = new VBox(10);
        resultsContainer.getStyleClass().add("results-container");
        Label resultsLabel = new Label("Solución del Sistema:");
        resultsContainer.getChildren().add(resultsLabel);

        // Ensamblar panel
        panel.getChildren().addAll(
            new Label("Sistema de Ecuaciones No Lineales:"),
            functionsContainer,
            new Separator(),
            inputGrid,
            buttonContainer,
            new Label("Proceso Iterativo:"),
            resultsTable,
            new Separator(),
            resultsContainer
        );

        // Manejadores de eventos
        btnCalculate.setOnAction(e -> solveSystem());

        return panel;
    }

    private VBox createFunctionsContainer() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(10));
        
        HBox f1Container = new HBox(10);
        Label f1Label = new Label("f₁(x,y) =");
        f1Field = new TextField();
        f1Field.setPromptText("Ej: x + 3*log(x) - y^2");
        f1Field.getStyleClass().add("equation-field");
        f1Field.setPrefWidth(400);
        f1Container.getChildren().addAll(f1Label, f1Field);
        
        HBox f2Container = new HBox(10);
        Label f2Label = new Label("f₂(x,y) =");
        f2Field = new TextField();
        f2Field.setPromptText("Ej: 2*x^2 - x*y - 5*x + 1");
        f2Field.getStyleClass().add("equation-field");
        f2Field.setPrefWidth(400);
        f2Container.getChildren().addAll(f2Label, f2Field);
        
        container.getChildren().addAll(f1Container, f2Container);
        return container;
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));
        
        x0Field = new TextField();
        y0Field = new TextField();
        tolField = new TextField();
        
        x0Field.setPromptText("Valor inicial de x");
        y0Field.setPromptText("Valor inicial de y");
        tolField.setPromptText("Tolerancia (0.01)");
        
        // Establecer valores predeterminados
        tolField.setText("0.000001");
        
        grid.addRow(0, new Label("Valor inicial x₀:"), x0Field, new Label("Valor inicial y₀:"), y0Field);
        grid.addRow(1, new Label("Tolerancia:"), tolField);
        
        return grid;
    }

    private TableView<List<String>> createResultsTable() {
        TableView<List<String>> table = new TableView<>();
        
        String[] columns = {"Iter", "xᵢ", "yᵢ", "f₁(xᵢ,yᵢ)", "f₂(xᵢ,yᵢ)", "∂f₁/∂x", "∂f₁/∂y", "∂f₂/∂x", "∂f₂/∂y", "Δx", "Δy", "Error x", "Error y"};
        
        for (int i = 0; i < columns.length; i++) {
            final int columnIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(columns[i]);
            column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().get(columnIndex)
            ));
            // Ajustar anchos de columna
            if (columns[i].equals("Iter")) {
                column.setPrefWidth(50);
            } else if (columns[i].contains("∂")) {
                column.setPrefWidth(90);
            } else {
                column.setPrefWidth(80);
            }
            table.getColumns().add(column);
        }
        
        table.setPrefHeight(300);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Método de Newton-Raphson Multivariable");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de Newton-Raphson Multivariable\n\n" +
            "El método de Newton-Raphson multivariable es un algoritmo iterativo para encontrar las raíces de un sistema de ecuaciones no lineales.\n\n" +
            "Algoritmo:\n" +
            "1) Definir las condiciones iniciales más próximas a la raíz.\n" +
            "2) Evaluar en las condiciones iniciales: f₁, f₂, ∂f₁/∂x, ∂f₁/∂y, ∂f₂/∂x, ∂f₂/∂y.\n" +
            "3) Encontrar Δx y Δy con las siguientes fórmulas:\n\n" +
            "   Δx = [-f₁·(∂f₂/∂y) + f₂·(∂f₁/∂y)] / [(∂f₁/∂x)·(∂f₂/∂y) - (∂f₂/∂x)·(∂f₁/∂y)]\n\n" +
            "   Δy = [-f₂·(∂f₁/∂x) + f₁·(∂f₂/∂x)] / [(∂f₁/∂x)·(∂f₂/∂y) - (∂f₂/∂x)·(∂f₁/∂y)]\n\n" +
            "4) Calcular los nuevos valores de x y y:\n" +
            "   xᵢ₊₁ = xᵢ + Δx\n" +
            "   yᵢ₊₁ = yᵢ + Δy\n\n" +
            "5) Repetir el proceso hasta que los errores en x e y sean menores o iguales a la tolerancia especificada.\n\n" +
            "Ventajas:\n" +
            "• Convergencia cuadrática cerca de la solución\n" +
            "• Eficiente para sistemas bien condicionados\n\n" +
            "Limitaciones:\n" +
            "• Requiere cálculo de derivadas parciales\n" +
            "• La convergencia depende de los valores iniciales\n" +
            "• Puede diverger si el jacobiano es singular"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void solveSystem() {
        try {
            // Limpiar resultados anteriores
            if (resultsContainer.getChildren().size() > 1) {
                resultsContainer.getChildren().subList(1, resultsContainer.getChildren().size()).clear();
            }

            // Obtener valores iniciales
            validateInputs();
            double x0 = Double.parseDouble(x0Field.getText().trim());
            double y0 = Double.parseDouble(y0Field.getText().trim());
            double tol = Double.parseDouble(tolField.getText().trim());

            // Definir las funciones y sus derivadas parciales
            String f1Expr = f1Field.getText().trim();
            String f2Expr = f2Field.getText().trim();

            // Resolver el sistema
            double[] solution = solveWithNewtonRaphson(f1Expr, f2Expr, x0, y0, tol);

            // Mostrar la solución
            HBox solutionBox = new HBox(20);
            solutionBox.setAlignment(Pos.CENTER_LEFT);
            solutionBox.setPadding(new Insets(10));

            Label xLabel = new Label("x = ");
            TextField xResult = new TextField(df.format(solution[0]));
            xResult.setEditable(false);
            xResult.getStyleClass().add("result-field");
            xResult.setPrefWidth(150);

            Label yLabel = new Label("y = ");
            TextField yResult = new TextField(df.format(solution[1]));
            yResult.setEditable(false);
            yResult.getStyleClass().add("result-field");
            yResult.setPrefWidth(150);

            Label iterLabel = new Label("Iteraciones: ");
            TextField iterResult = new TextField(String.valueOf((int)solution[2]));
            iterResult.setEditable(false);
            iterResult.getStyleClass().add("result-field");
            iterResult.setPrefWidth(80);

            solutionBox.getChildren().addAll(xLabel, xResult, yLabel, yResult, iterLabel, iterResult);
            resultsContainer.getChildren().add(solutionBox);

        } catch (Exception e) {
            showErrorDialog("Error al resolver", e.getMessage());
        }
    }

    private double[] solveWithNewtonRaphson(String f1Expr, String f2Expr, double x0, double y0, double tol) {
        ObservableList<List<String>> steps = FXCollections.observableArrayList();
        
        // Variables para la iteración
        double x = x0;
        double y = y0;
        double deltaX, deltaY;
        double xNew, yNew;
        double errorX = 1.0;
        double errorY = 1.0;
        int iter = 0;
    
        // Crear expresiones para f1, f2
        Expression f1 = new ExpressionBuilder(f1Expr)
            .variables("x", "y")
            .build();
        
        Expression f2 = new ExpressionBuilder(f2Expr)
            .variables("x", "y")
            .build();
        
        // Derivadas parciales (cálculo numérico usando diferencias centrales)
        double h = 1e-6; // Paso para la aproximación de la derivada
        
        // Límite de seguridad para evitar bucles infinitos
        final int SAFETY_LIMIT = 100;
        
        // Iteramos hasta que se alcance la tolerancia o el límite de seguridad
        while ((Math.abs(errorX) > tol || Math.abs(errorY) > tol) && iter < SAFETY_LIMIT) {
            // 1. Evaluar las funciones en los valores actuales de x,y
            double f1Val = evaluateExpression(f1, x, y);
            double f2Val = evaluateExpression(f2, x, y);
            
            // 2. Calcular las derivadas parciales en los valores actuales de x,y
            double df1dx = (evaluateExpression(f1, x + h, y) - evaluateExpression(f1, x - h, y)) / (2 * h);
            double df1dy = (evaluateExpression(f1, x, y + h) - evaluateExpression(f1, x, y - h)) / (2 * h);
            double df2dx = (evaluateExpression(f2, x + h, y) - evaluateExpression(f2, x - h, y)) / (2 * h);
            double df2dy = (evaluateExpression(f2, x, y + h) - evaluateExpression(f2, x, y - h)) / (2 * h);
            
            // 3. Calcular el determinante del Jacobiano
            double det = df1dx * df2dy - df2dx * df1dy;
            
            if (Math.abs(det) < 1e-10) {
                throw new ArithmeticException("El Jacobiano es singular o mal condicionado en x=" + x + ", y=" + y);
            }
            
            // 4. Calcular los incrementos Δx y Δy según las fórmulas del PDF
            deltaX = (-f1Val * df2dy + f2Val * df1dy) / det;
            deltaY = (-f2Val * df1dx + f1Val * df2dx) / det;
            
            // 5. Calcular los nuevos valores de x e y
            xNew = x + deltaX;
            yNew = y + deltaY;
            
            // 6. Calcular los errores (usando el error relativo cuando sea posible)
            errorX = Math.abs(deltaX);
            errorY = Math.abs(deltaY);
            
            // 7. Agregar esta iteración a la tabla de resultados
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(iter + 1));
            row.add(df.format(x));       // xᵢ
            row.add(df.format(y));       // yᵢ
            row.add(df.format(f1Val));   // f₁(xᵢ,yᵢ)
            row.add(df.format(f2Val));   // f₂(xᵢ,yᵢ)
            row.add(df.format(df1dx));   // ∂f₁/∂x
            row.add(df.format(df1dy));   // ∂f₁/∂y
            row.add(df.format(df2dx));   // ∂f₂/∂x
            row.add(df.format(df2dy));   // ∂f₂/∂y
            row.add(df.format(deltaX));  // Δx
            row.add(df.format(deltaY));  // Δy
            row.add(df.format(errorX));  // Error x
            row.add(df.format(errorY));  // Error y
            steps.add(row);
            
            // 8. Actualizar x e y para la siguiente iteración
            x = xNew;
            y = yNew;
            iter++;
        }
        
        // Verificar si se alcanzó el límite de seguridad
        if (iter >= SAFETY_LIMIT) {
            showWarningDialog("Advertencia", "El método no ha convergido después de " + SAFETY_LIMIT + 
                              " iteraciones. Posible divergencia o convergencia muy lenta.");
        }
        
        // Mostrar los resultados en la tabla
        resultsTable.setItems(steps);
        
        // Devolver la solución encontrada y el número de iteraciones
        return new double[]{x, y, iter};
    }
    
    // Método auxiliar para evaluar una expresión con valores dados de x e y
    private double evaluateExpression(Expression expr, double x, double y) {
        return expr.setVariable("x", x).setVariable("y", y).evaluate();
    }

    private void validateInputs() throws IllegalArgumentException {
        if (f1Field.getText().trim().isEmpty() || f2Field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar ambas ecuaciones del sistema");
        }
        
        if (x0Field.getText().trim().isEmpty() || y0Field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar los valores iniciales para x e y");
        }
        
        try {
            Double.parseDouble(x0Field.getText().trim());
            Double.parseDouble(y0Field.getText().trim());
            Double.parseDouble(tolField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los valores numéricos son inválidos");
        }
    }

    private void loadExample() {
        // Cargar el ejemplo del PDF
        f1Field.setText("x + 3*log(x) - y^2");
        f2Field.setText("2*x^2 - x*y - 5*x + 1");
        x0Field.setText("2");
        y0Field.setText("1");
        tolField.setText("0.000001");
    }

    private void clearInputs() {
        f1Field.clear();
        f2Field.clear();
        x0Field.clear();
        y0Field.clear();
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