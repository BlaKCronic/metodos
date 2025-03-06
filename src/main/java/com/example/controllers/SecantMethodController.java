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
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> functionSeries;

    public void show() {
        Stage stage = new Stage();

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Componentes principales
        HBox mainContent = new HBox(15);
        VBox inputSection = new VBox(10);
        VBox chartSection = new VBox(10);

        // Componentes de entrada
        equationField = new TextField();
        TextField x0Field = new TextField();
        TextField x1Field = new TextField();
        TextField toleranceField = new TextField();
        TextField maxIterationsField = new TextField();

        Button calculateBtn = new Button("Calcular");
        TableView<List<String>> table = createTable();

        // Configurar gráfica
        setupChart();

        // Configurar teclado matemático
        GridPane mathKeyboard = createMathKeyboard();

        // Organizar layout
        inputSection.getChildren().addAll(
                createInputForm(equationField, x0Field, x1Field, toleranceField, maxIterationsField),
                mathKeyboard,
                calculateBtn,
                table
        );

        chartSection.getChildren().addAll(new Label("Gráfica de la función"), chart);
        mainContent.getChildren().addAll(inputSection, chartSection);
        root.getChildren().add(mainContent);

        calculateBtn.setOnAction(e -> {
            try {
                updateChart(equationField.getText());
                performSecantMethod(
                        equationField.getText(),
                        Double.parseDouble(x0Field.getText()),
                        Double.parseDouble(x1Field.getText()),
                        Double.parseDouble(toleranceField.getText()),
                        Integer.parseInt(maxIterationsField.getText()),
                        table
                );
            } catch (Exception ex) {
                showAlert("Error en los datos de entrada");
            }
        });

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void setupChart() {
        NumberAxis xAxis = new NumberAxis(-10, 10, 1);
        NumberAxis yAxis = new NumberAxis(-10, 10, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefSize(500, 400);
        chart.setTitle("Función");
        chart.setLegendVisible(false);
        functionSeries = new XYChart.Series<>();
        chart.getData().add(functionSeries);
    }

    private void updateChart(String equation) {
        functionSeries.getData().clear();
        try {
            Expression expr = new ExpressionBuilder(equation)
                    .variable("x")
                    .build();

            for (double x = -10; x <= 10; x += 0.1) {
                try {
                    double y = evaluate(expr, x);
                    functionSeries.getData().add(new XYChart.Data<>(x, y));
                } catch (ArithmeticException e) {
                    // Puntos no válidos
                }
            }
        } catch (Exception e) {
            showAlert("Error al graficar la ecuación");
        }
    }

    private GridPane createMathKeyboard() {
        GridPane keyboard = new GridPane();
        keyboard.setVgap(5);
        keyboard.setHgap(5);

        String[][] buttons = {
            {"sin(", "cos(", "tan(", "asin(", "acos(", "atan("},
            {"log(", "ln(", "sqrt(", "abs(", "^", ")"},
            {"π", "e", "(", "7", "8", "9"},
            {"4", "5", "6", "+", "-", "*"},
            {"1", "2", "3", ".", "x", "/"},
            {"0", "←", "C", "E", "=", ","}
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
            default:
                equationField.setText(current + text);
                break;
        }
    }

    private TableView<List<String>> createTable() {
        TableView<List<String>> table = new TableView<>();
        String[] columns = {"Xi-1", "Xi", "f(Xi-1)", "f(Xi)", "Xi+1", "Error"};

        for (int i = 0; i < columns.length; i++) {
            TableColumn<List<String>, String> col = new TableColumn<>(columns[i]);
            final int colIndex = i;
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
            table.getColumns().add(col);
        }
        return table;
    }

    private void performSecantMethod(String equation, double x0, double x1, double tolerance, int maxIterations, TableView<List<String>> table) {
        ObservableList<List<String>> data = FXCollections.observableArrayList();

        try {
            Expression expr = new ExpressionBuilder(equation)
                    .variable("x")
                    .build()
                    .setVariable("π", Math.PI)
                    .setVariable("e", Math.E);

            double xiPrev = x0;
            double xi = x1;
            int iterations = 0;

            while (iterations < maxIterations) {
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

                if (error < tolerance) break;

                xiPrev = xi;
                xi = xiPlus1;
                iterations++;
            }

            table.setItems(data);
        } catch (Exception e) {
            showAlert("Error en la ecuación: " + e.getMessage());
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
        alert.setContentText(message);
        alert.show();
    }

    private GridPane createInputForm(TextField... fields) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        String[] labels = {"Ecuación", "Xi-1", "Xi", "Tolerancia", "Max Iteraciones"};

        for (int i = 0; i < labels.length; i++) {
            grid.add(new Label(labels[i]), 0, i);
            grid.add(fields[i], 1, i);
        }
        return grid;
    }
}