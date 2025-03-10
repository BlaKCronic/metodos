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
        mainStage.setTitle("Método de la Secante");

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.65);

        // Sección izquierda (controles y resultados)
        VBox leftSection = new VBox(10);
        leftSection.setPadding(new Insets(15));

        // Componentes de entrada
        HBox equationBox = new HBox(10);
        equationField = new TextField();
        equationField.setPromptText("Ingrese la función (ej: x^2 - 4)");
        
        Button exampleBtn = new Button("Ejemplo");
        exampleBtn.getStyleClass().add("example-button");
        exampleBtn.setOnAction(e -> loadExample());
        
        equationBox.getChildren().addAll(equationField, exampleBtn);

        // Teclado matemático
        GridPane mathKeyboard = createMathKeyboard();
        
        TextField x0Field = new TextField();
        x0Field.setPromptText("Xi-1 inicial (ej: 2)");
        TextField x1Field = new TextField();
        x1Field.setPromptText("Xi inicial (ej: 3)");
        TextField toleranceField = new TextField();
        toleranceField.setPromptText("Tolerancia (ej: 0.0001)");

        Button calculateBtn = new Button("Calcular");
        calculateBtn.getStyleClass().add("calculate-btn");
        
        Button showGraphBtn = new Button("Mostrar Gráfica");
        showGraphBtn.getStyleClass().add("graph-btn");
        showGraphBtn.setDisable(true);
        
        TableView<List<String>> table = createTable();
        
        // Resultado final
        HBox resultBox = new HBox(10);
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.getStyleClass().add("result-field");
        resultBox.getChildren().addAll(new Label("Raíz aproximada:"), resultField);

        // Organizar sección izquierda
        leftSection.getChildren().addAll(
                createInputForm(equationBox, x0Field, x1Field, toleranceField),
                mathKeyboard,
                calculateBtn,
                table,
                resultBox,
                showGraphBtn
        );

        // Sección derecha (descripción del método)
        VBox rightSection = new VBox(10);
        rightSection.setPadding(new Insets(5));
        rightSection.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Teoría del Método de la Secante");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea description = new TextArea(
            "En análisis numérico el método de la secante es un método para encontrar los ceros\n" +
            "de una función de forma iterativa.\n\n" +
            
            "Es una variación del método de Newton-Raphson donde en vez de calcular la derivada\n" +
            "de la función en el punto de estudio, se aproxima la pendiente a la recta que une\n" + 
            "la función evaluada en el punto de estudio y en el punto de la iteración anterior.\n\n" +
            
            "Fórmula de aproximación:\n" +
            "xₙ₊₁ = xₙ - f(xₙ) * (xₙ - xₙ₋₁) / (f(xₙ) - f(xₙ₋₁))\n\n" +
            
            "Características clave:\n" +
            "• Alternativa al método de Newton-Raphson\n" +
            "• Elimina necesidad de calcular derivadas\n" +
            "• Coste computacional más bajo\n" +
            "• Convergencia superlineal (orden 1.618)\n\n" +
            
            "Ventajas principales:\n" +
            "- Ideal cuando la derivada es compleja/costosa\n" +
            "- Más eficiente que métodos como bisección\n" +
            "- Implementación relativamente simple\n\n" +
            
            "Consideraciones importantes:\n" +
            "• Requiere dos aproximaciones iniciales\n" +
            "• Sensible a la selección de puntos iniciales\n" +
            "• Posible divergencia en funciones no suaves\n" +
            "• Criterio de parada basado en tolerancia"
        );
        description.setEditable(false);
        description.setWrapText(true);
        description.setStyle("-fx-font-family: 'Segoe UI';" +
                            "-fx-font-size: 13;" +
                            "-fx-text-fill: #34495e;" +
                            "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;" +
                            "-fx-pref-height: 600;"
                            );

        rightSection.getChildren().addAll(title, description);
        mainSplitPane.getItems().addAll(leftSection, rightSection);

        // Configurar eventos
        calculateBtn.setOnAction(e -> {
            try {
                validateEmptyFields(equationField, x0Field, x1Field, toleranceField);
                currentEquation = equationField.getText();
                performSecantMethod(
                        currentEquation,
                        parseDouble(x0Field.getText(), "Xi-1"),
                        parseDouble(x1Field.getText(), "Xi"),
                        parseDouble(toleranceField.getText(), "Tolerancia"),
                        table
                );
                showGraphBtn.setDisable(false);
            } catch (IllegalArgumentException ex) {
                showAlert(ex.getMessage());
            } catch (Exception ex) {
                showAlert("Error inesperado: " + ex.getMessage());
            }
        });

        showGraphBtn.setOnAction(e -> {
            if (!currentEquation.isEmpty()) {
                showGraphWindow(currentEquation);
            }
        });

        Scene scene = new Scene(mainSplitPane, 1300, 750);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private GridPane createMathKeyboard() {
        GridPane keyboard = new GridPane();
        keyboard.setVgap(5);
        keyboard.setHgap(5);
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
        btn.getStyleClass().add("keyboard-button");
        if (text.matches("[a-zA-Z]+")) btn.getStyleClass().add("function-button");
        
        btn.setOnAction(e -> handleButtonAction(text));
        return btn;
    }

    private void handleButtonAction(String text) {
        String current = equationField.getText();
        switch (text) {
            case "←":
                if (!current.isEmpty()) {
                    equationField.setText(current.substring(0, current.length() - 1));
                }
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
                equationField.setText(current + text);
                break;
        }
    }

    private void loadExample() {
        equationField.setText("x^2 - 4");
    }

    private void showGraphWindow(String equation) {
        Stage graphStage = new Stage();
        graphStage.setTitle("Gráfica de la Función");
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        LineChart<Number, Number> chart = createChart(equation);
        root.getChildren().addAll(chart);

        Scene scene = new Scene(root, 600, 500);
        graphStage.setScene(scene);
        graphStage.show();
    }

    private LineChart<Number, Number> createChart(String equation) {
        NumberAxis xAxis = new NumberAxis(-10, 10, 1);
        NumberAxis yAxis = new NumberAxis(-10, 10, 1);
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Función: " + equation);
        chart.setLegendVisible(false);
        
        // Configurar zoom y desplazamiento
        chart.setAnimated(false);
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        
        chart.setOnScroll(event -> {
            double zoomFactor = 1.05;
            if (event.getDeltaY() < 0) zoomFactor = 0.95;
            
            double xMin = xAxis.getLowerBound();
            double xMax = xAxis.getUpperBound();
            double yMin = yAxis.getLowerBound();
            double yMax = yAxis.getUpperBound();
            
            double mouseX = event.getX();
            double mouseY = event.getY();
            
            double xValue = xAxis.getValueForDisplay(mouseX).doubleValue();
            double yValue = yAxis.getValueForDisplay(mouseY).doubleValue();
            
            xAxis.setLowerBound(xValue - (xValue - xMin) * zoomFactor);
            xAxis.setUpperBound(xValue + (xMax - xValue) * zoomFactor);
            yAxis.setLowerBound(yValue - (yValue - yMin) * zoomFactor);
            yAxis.setUpperBound(yValue + (yMax - yValue) * zoomFactor);
            
            event.consume();
        });
        
        chart.setOnMousePressed(event -> {
            xOffset[0] = event.getX();
            yOffset[0] = event.getY();
        });
        
        chart.setOnMouseDragged(event -> {
            double deltaX = (event.getX() - xOffset[0]) / chart.getWidth();
            double deltaY = (event.getY() - yOffset[0]) / chart.getHeight();
            
            double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();
            
            xAxis.setLowerBound(xAxis.getLowerBound() - deltaX * xRange);
            xAxis.setUpperBound(xAxis.getUpperBound() - deltaX * xRange);
            yAxis.setLowerBound(yAxis.getLowerBound() + deltaY * yRange);
            yAxis.setUpperBound(yAxis.getUpperBound() + deltaY * yRange);
            
            xOffset[0] = event.getX();
            yOffset[0] = event.getY();
            event.consume();
        });

        XYChart.Series<Number, Number> functionSeries = new XYChart.Series<>();
        
        try {
            Expression expr = new ExpressionBuilder(equation)
                    .variable("x")
                    .build();

            for (double x = -10; x <= 10; x += 0.1) {
                try {
                    double y = evaluate(expr, x);
                    functionSeries.getData().add(new XYChart.Data<>(x, y));
                } catch (ArithmeticException e) {
                    // Ignorar puntos no válidos
                }
            }
        } catch (Exception e) {
            showAlert("Error al graficar: " + e.getMessage());
        }
        
        chart.getData().add(functionSeries);
        return chart;
    }

    private GridPane createInputForm(HBox equationBox, TextField... fields) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        String[] labels = {"Ecuación", "Xi-1", "Xi", "Tolerancia"};

        grid.add(new Label(labels[0]), 0, 0);
        grid.add(equationBox, 1, 0);
        
        for (int i = 1; i < labels.length; i++) {
            grid.add(new Label(labels[i]), 0, i);
            grid.add(fields[i-1], 1, i);
        }
        return grid;
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

            validateInputValues(x0, x1, tolerance);

            double xiPrev = x0;
            double xi = x1;
            double finalRoot = Double.NaN;
            boolean converged = false;
            int iteration = 0;
            final int MAX_SAFE_ITERATIONS = 1000;

            while (!converged && iteration < MAX_SAFE_ITERATIONS) {
                double fxiPrev = evaluate(expr, xiPrev);
                double fxi = evaluate(expr, xi);

                if (Math.abs(fxiPrev - fxi) < 1e-12) break;

                double xiPlus1 = xi - (fxi * (xiPrev - xi)) / (fxiPrev - fxi);
                double error = Math.abs((xiPlus1 - xi) / xiPlus1);

                List<String> row = new ArrayList<>();
                row.add(roundSixDecimals(xiPrev));
                row.add(roundSixDecimals(xi));
                row.add(roundSixDecimals(fxiPrev));
                row.add(roundSixDecimals(fxi));
                row.add(roundSixDecimals(xiPlus1));
                row.add(roundSixDecimals(error));
                data.add(row);

                if (error < tolerance) {
                    finalRoot = xiPlus1;
                    converged = true;
                }

                xiPrev = xi;
                xi = xiPlus1;
                iteration++;
            }

            table.setItems(data);
            
            if (!Double.isNaN(finalRoot)) {
                resultField.setText(roundSixDecimals(finalRoot));
            } else {
                resultField.setText("No convergió en " + iteration + " iteraciones");
            }

        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage());
        }
    }

    private double evaluate(Expression expr, double x) {
        expr.setVariable("x", x);
        return expr.evaluate();
    }

    private String roundSixDecimals(double value) {
        return String.format("%.6f", Math.round(value * 1e6) / 1e6);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void validateEmptyFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                throw new IllegalArgumentException("Todos los campos deben estar completos");
            }
        }
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido para " + fieldName + ": " + value);
        }
    }

    private void validateInputValues(double x0, double x1, double tolerance) {
        if (Double.isNaN(x0) || Double.isNaN(x1)) {
            throw new IllegalArgumentException("Valores iniciales inválidos");
        }
        if (tolerance <= 0) {
            throw new IllegalArgumentException("La tolerancia debe ser mayor que cero");
        }
    }

    private TableView<List<String>> createTable() {
        TableView<List<String>> table = new TableView<>();
        String[] columns = {"Xi-1", "Xi", "f(Xi-1)", "f(Xi)", "Xi+1", "Error"};

        for (int i = 0; i < columns.length; i++) {
            TableColumn<List<String>, String> col = new TableColumn<>(columns[i]);
            final int colIndex = i;
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
            col.setPrefWidth(120);
            table.getColumns().add(col);
        }
        return table;
    }
}