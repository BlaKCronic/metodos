package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SecantMethodController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TextField equationField;
    private TextField resultField;
    private String currentEquation = "";

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de la Secante - Análisis Numérico");

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

        // Sección de entrada de ecuación
        HBox equationContainer = new HBox(10);
        equationField = new TextField();
        equationField.setPromptText("Ej: x^3 + 2*cos(x) - 5");
        equationField.getStyleClass().add("equation-field");
        
        Button btnExample = new Button("Ejemplo");
        btnExample.getStyleClass().add("example-btn");
        btnExample.setOnAction(e -> loadExample());
        
        equationContainer.getChildren().addAll(equationField, btnExample);

        // Teclado matemático interactivo
        GridPane mathKeyboard = createMathKeyboard();

        // Campos de entrada numéricos
        GridPane inputGrid = createInputGrid();

        // Botones principales
        HBox buttonContainer = new HBox(10);
        Button btnCalculate = new Button("Calcular");
        btnCalculate.getStyleClass().add("calculate-btn");
        
        Button btnGraph = new Button("Mostrar Gráfica");
        btnGraph.getStyleClass().add("graph-btn");
        btnGraph.setDisable(true);

        buttonContainer.getChildren().addAll(btnCalculate, btnGraph);

        // Tabla de resultados
        TableView<List<String>> resultsTable = createResultsTable();

        // Resultado final
        HBox resultContainer = new HBox(10);
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.getStyleClass().add("result-field");
        resultContainer.getChildren().addAll(new Label("Raíz aproximada:"), resultField);

        // Ensamblar panel
        panel.getChildren().addAll(
            new Label("Ingrese la función:"),
            equationContainer,
            mathKeyboard,
            new Separator(),
            inputGrid,
            buttonContainer,
            new Label("Proceso Iterativo:"),
            resultsTable,
            resultContainer
        );

        // Event handlers
        btnCalculate.setOnAction(e -> handleCalculation(resultsTable, btnGraph));
        btnGraph.setOnAction(e -> showGraphWindow());

        return panel;
    }

    private GridPane createMathKeyboard() {
        GridPane keyboard = new GridPane();
        keyboard.setHgap(6);
        keyboard.setVgap(6);
        keyboard.setPadding(new Insets(10));

        String[][] buttons = {
            {"sin(", "cos(", "tan(", "√(", "π", "e"},
            {"log(", "ln(", "^", "(", ")", "abs("},
            {"7", "8", "9", "+", "-", "←"},
            {"4", "5", "6", "*", "/", "C"},
            {"1", "2", "3", ".", "x", "="}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                Button btn = createKeyboardButton(buttons[row][col]);
                keyboard.add(btn, col, row);
            }
        }
        return keyboard;
    }

    private Button createKeyboardButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("key-btn", 
            text.matches("[+\\-*/^=]") ? "operator-key" : 
            text.matches("[a-zA-Z]+") ? "function-key" : "number-key");
        
        btn.setOnAction(e -> handleKeyboardInput(text));
        return btn;
    }

    private void handleKeyboardInput(String key) {
        String current = equationField.getText();
        switch (key) {
            case "←": 
                if (!current.isEmpty()) equationField.setText(current.substring(0, current.length()-1));
                break;
            case "C": 
                equationField.clear();
                break;
            case "π": 
                equationField.setText(current + Math.PI);
                break;
            case "e": 
                equationField.setText(current + Math.E);
                break;
            case "√(": 
                equationField.setText(current + "sqrt(");
                break;
            default: 
                equationField.setText(current + key);
        }
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        
        TextField x0Field = new TextField();
        TextField x1Field = new TextField();
        TextField tolField = new TextField();
        
        x0Field.setPromptText("Xi-1 inicial");
        x1Field.setPromptText("Xi inicial");
        tolField.setPromptText("Tolerancia (0.001)");
        
        grid.addRow(0, new Label("Valor inicial Xi-1:"), x0Field);
        grid.addRow(1, new Label("Valor inicial Xi:"), x1Field);
        grid.addRow(2, new Label("Tolerancia máxima:"), tolField);
        
        return grid;
    }

    private TableView<List<String>> createResultsTable() {
        TableView<List<String>> table = new TableView<>();
        String[] columns = {"Iter", "Xi-1", "Xi", "f(Xi-1)", "f(Xi)", "Xi+1", "Error"};
        
        for (String col : columns) {
            TableColumn<List<String>, String> column = new TableColumn<>(col);
            column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().get(table.getColumns().indexOf(column))
            ));
            column.setPrefWidth(col.equals("Iter") ? 60 : 100);
            table.getColumns().add(column);
        }
        table.setPrefHeight(350);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Fundamentos del Método");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "Método de la Secante\n\n" +
            "Algoritmo iterativo para encontrar raíces de funciones continuas.\n\n" +
            "Fórmula de iteración:\n" +
            "xₙ₊₁ = xₙ - f(xₙ)[(xₙ - xₙ₋₁)/(f(xₙ) - f(xₙ₋₁))]\n\n" +
            "Características clave:\n" +
            "• Velocidad de convergencia: 1.618 (proporción áurea)\n" +
            "• No requiere cálculo de derivadas\n" +
            "• Necesita dos aproximaciones iniciales\n" +
            "• Criterio de parada por error relativo\n\n" +
            "Recomendaciones prácticas:\n" +
            "- Usar valores iniciales cercanos a la raíz\n" +
            "- Verificar continuidad en el intervalo\n" +
            "- Tolerancia típica: 1e-5 a 1e-8\n" +
            "- Límite de iteraciones: 100-200"
        );
        content.getStyleClass().add("theory-content");
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void handleCalculation(TableView<List<String>> table, Button graphBtn) {
        try {
            TextField x0Field = (TextField) ((GridPane) table.getParent().getChildrenUnmodifiable().get(4)).getChildren().get(1);
            TextField x1Field = (TextField) ((GridPane) table.getParent().getChildrenUnmodifiable().get(4)).getChildren().get(3);
            TextField tolField = (TextField) ((GridPane) table.getParent().getChildrenUnmodifiable().get(4)).getChildren().get(5);

            validateInputs(x0Field, x1Field, tolField);
            currentEquation = equationField.getText();
            
            performSecantMethod(
                currentEquation,
                parseDouble(x0Field.getText()),
                parseDouble(x1Field.getText()),
                parseDouble(tolField.getText()),
                table
            );
            
            graphBtn.setDisable(false);
        } catch (Exception e) {
            showErrorDialog("Error de entrada", e.getMessage());
        }
    }

    private void performSecantMethod(String equation, double x0, double x1, double tolerance, TableView<List<String>> table) {
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        resultField.clear();

        try {
            Expression expr = new ExpressionBuilder(equation)
                .variable("x")
                .build()
                .setVariable("π", Math.PI)
                .setVariable("e", Math.E);

            double xiPrev = x0;
            double xi = x1;
            double root = Double.NaN;
            int iter = 0;
            final int MAX_ITER = 200;

            while (iter < MAX_ITER) {
                double fPrev = evaluate(expr, xiPrev);
                double fCurrent = evaluate(expr, xi);
                
                if (Math.abs(fCurrent - fPrev) < 1e-15) break;
                
                double xiPlus1 = xi - fCurrent * (xi - xiPrev) / (fCurrent - fPrev);
                double error = Math.abs((xiPlus1 - xi) / xiPlus1);
                
                data.add(createTableRow(iter+1, xiPrev, xi, fPrev, fCurrent, xiPlus1, error));
                
                if (error < tolerance) {
                    root = xiPlus1;
                    break;
                }
                
                xiPrev = xi;
                xi = xiPlus1;
                iter++;
            }

            table.setItems(data);
            resultField.setText(iter < MAX_ITER ? df.format(root) : "No converge en " + iter + " iteraciones");
            
        } catch (Exception e) {
            showErrorDialog("Error de cálculo", e.getMessage());
        }
    }

    private List<String> createTableRow(int iter, double... values) {
        List<String> row = new ArrayList<>();
        row.add(String.valueOf(iter));
        for (double val : values) row.add(df.format(val));
        return row;
    }

    private void showGraphWindow() {
        Stage graphStage = new Stage();
        graphStage.setTitle("Análisis Gráfico: " + currentEquation);

        LineChart<Number, Number> chart = createChart(currentEquation);
        chart.getStyleClass().add("chart-container");

        ScrollPane scroll = new ScrollPane(chart);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Scene scene = new Scene(scroll, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        graphStage.setScene(scene);
        graphStage.show();
    }

    private LineChart<Number, Number> createChart(String equation) {
        NumberAxis xAxis = new NumberAxis(-10, 10, 1);
        NumberAxis yAxis = new NumberAxis(-10, 10, 1);
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("f(x) = " + equation);

        try {
            Expression expr = new ExpressionBuilder(equation).variable("x").build();
            for (double x = -9.5; x <= 10; x += 0.15) {
                try {
                    double y = expr.setVariable("x", x).evaluate();
                    series.getData().add(new XYChart.Data<>(x, y));
                } catch (ArithmeticException e) { /* Ignorar puntos inválidos */ }
            }
        } catch (Exception e) {
            showErrorDialog("Error gráfico", "No se puede graficar la función");
        }
        
        chart.getData().add(series);
        return chart;
    }

    private double evaluate(Expression expr, double x) {
        expr.setVariable("x", x);
        return expr.evaluate();
    }

    private void validateInputs(TextField... fields) throws IllegalArgumentException {
        for (TextField field : fields) {
            if (field.getText().isBlank()) {
                throw new IllegalArgumentException("Complete todos los campos requeridos");
            }
        }
    }

    private double parseDouble(String text) throws NumberFormatException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Valor numérico inválido: " + text);
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadExample() {
        equationField.setText("x^3 - 2*x - 5");
    }
}