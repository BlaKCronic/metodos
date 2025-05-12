package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class BisectionMethodController {
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private TextField equationField;
    private TextField aField;
    private TextField bField;
    private TextField tolField;
    private TextField resultField;
    private TableView<List<String>> resultsTable;
    private String currentEquation = "";

    public void show() {
        Stage mainStage = new Stage();
        mainStage.setTitle("Método de Bisección - Análisis Numérico");

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
        equationField.setPromptText("Ej: x^3 - 2*x - 5");
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
        // El botón ya no estará deshabilitado
        
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("graph-btn");
        btnClear.setOnAction(e -> clearInputs());

        buttonContainer.getChildren().addAll(btnCalculate, btnGraph, btnClear);

        // Tabla de resultados
        resultsTable = createResultsTable();
        ScrollPane tableScrollPane = new ScrollPane(resultsTable);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setPrefHeight(300);
        tableScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Resultado final
        HBox resultContainer = new HBox(10);
        resultContainer.setAlignment(Pos.CENTER_LEFT);
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.getStyleClass().add("result-field");
        resultField.setPrefWidth(150);
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
            tableScrollPane,
            resultContainer
        );

        // Event handlers
        btnCalculate.setOnAction(e -> {
            try {
                validateInputs();
                currentEquation = equationField.getText();
                performBisectionMethod();
                // Eliminamos la línea que habilita el botón aquí
            } catch (Exception ex) {
                showErrorDialog("Error de entrada", ex.getMessage());
            }
        });
        
        btnGraph.setOnAction(e -> {
            try {
                // Antes de mostrar la gráfica, validamos que haya una ecuación
                if (equationField.getText().isBlank()) {
                    throw new IllegalArgumentException("Debe ingresar una función para graficar");
                }
                
                // Actualizamos la ecuación actual
                currentEquation = equationField.getText();
                
                // Si los campos a y b están vacíos, asignamos valores predeterminados
                if (aField.getText().isBlank()) {
                    aField.setText("-10");
                }
                if (bField.getText().isBlank()) {
                    bField.setText("10");
                }
                
                showGraphWindow();
            } catch (Exception ex) {
                showErrorDialog("Error de gráfica", ex.getMessage());
            }
        });

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
            text.matches("[a-zA-Z]+\\(|[a-zA-Z]+") ? "function-key" : "number-key");
        
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
        
        aField = new TextField();
        bField = new TextField();
        tolField = new TextField("0.0001");
        
        aField.setPromptText("Extremo inferior a");
        bField.setPromptText("Extremo superior b");
        tolField.setPromptText("Tolerancia (ej: 0.0001)");
        
        grid.addRow(0, new Label("Extremo inferior a:"), aField);
        grid.addRow(1, new Label("Extremo superior b:"), bField);
        grid.addRow(2, new Label("Tolerancia máxima:"), tolField);
        
        return grid;
    }

    private TableView<List<String>> createResultsTable() {
        TableView<List<String>> table = new TableView<>();
        String[] columns = {"Iter", "a", "b", "f(a)", "f(b)", "xr", "f(xr)", "Error(%)"};
        
        for (int i = 0; i < columns.length; i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(columns[i]);
            column.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
            column.setPrefWidth(columns[i].equals("Iter") ? 60 : 100);
            table.getColumns().add(column);
        }
        
        table.setPrefHeight(350);
        return table;
    }

    private VBox createTheoryPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("theory-panel");
        panel.setPadding(new Insets(25));

        Label title = new Label("Método de Bisección");
        title.getStyleClass().add("theory-title");

        TextArea content = new TextArea(
            "ALGORITMO DEL MÉTODO DE BISECCIÓN\n\n" +
            "El método de bisección es un algoritmo de búsqueda de raíces que divide repetidamente un intervalo a la mitad y luego selecciona el subintervalo donde la raíz debe estar.\n\n" +
            "Algoritmo:\n\n" +
            "1. Seleccionar los valores iniciales de a y b y evaluar f(a) y f(b) en este intervalo, de manera que la función cambie de signo. Establecer una tolerancia de error.\n\n" +
            "2. La primera aproximación de la raíz se calcula por medio de la siguiente ecuación:\n" +
            "   xr = (a + b)/2\n\n" +
            "3. Realizar las siguientes evaluaciones para determinar si se encontró la raíz o para saber en qué subintervalo se localiza:\n\n" +
            "   Si f(a) * f(xr) = 0, entonces la raíz es igual a xr y se terminan los cálculos.\n" +
            "   Si f(a) * f(xr) > 0, entonces la raíz se encuentra entre xr y b. Hacer a = xr y pasar al punto 4.\n" +
            "   Si f(a) * f(xr) < 0, entonces la raíz se encuentra entre xr y a. Hacer b = xr y pasar al punto 4.\n\n" +
            "4. Calcular el nuevo xr con la ecuación del punto número 2.\n\n" +
            "5. Calcular el error aproximado, con la siguiente ecuación, para decidir si la nueva aproximación cumple con el criterio de error establecido. Si es así, los cálculos terminan, en caso contrario se regresa al punto número 3.\n" +
            "   ep = |xr actual - xr anterior|/|xr actual| * 100\n\n" +
            "Ventajas:\n" +
            "• Simple y robusto\n" +
            "• Siempre converge a una raíz si f(a) y f(b) tienen signos opuestos\n" +
            "• Fácil de implementar\n\n" +
            "Desventajas:\n" +
            "• Convergencia relativamente lenta\n" +
            "• Requiere que la función cambie de signo alrededor de la raíz\n" +
            "• No detecta raíces múltiples"
        );
        content.getStyleClass().add("theory-content");
        content.setEditable(false);
        content.setWrapText(true);
        
        panel.getChildren().addAll(title, content);
        return panel;
    }

    private void validateInputs() throws IllegalArgumentException {
        if (equationField.getText().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar una función");
        }
        
        if (aField.getText().isBlank() || bField.getText().isBlank() || tolField.getText().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos requeridos");
        }
        
        try {
            double a = Double.parseDouble(aField.getText());
            double b = Double.parseDouble(bField.getText());
            double tol = Double.parseDouble(tolField.getText());
            
            if (a >= b) {
                throw new IllegalArgumentException("El valor de 'a' debe ser menor que 'b'");
            }
            
            if (tol <= 0) {
                throw new IllegalArgumentException("La tolerancia debe ser un valor positivo");
            }
            
            // Verificar que la función cambia de signo en el intervalo [a, b]
            Expression expr = new ExpressionBuilder(equationField.getText())
                .variable("x")
                .build();
            
            double fa = expr.setVariable("x", a).evaluate();
            double fb = expr.setVariable("x", b).evaluate();
            
            if (fa * fb >= 0) {
                throw new IllegalArgumentException("La función debe cambiar de signo en el intervalo [a, b]");
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingrese valores numéricos válidos");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al evaluar la función: " + e.getMessage());
        }
    }

    private void performBisectionMethod() {
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        resultsTable.getItems().clear();
        resultField.clear();
        
        try {
            double a = Double.parseDouble(aField.getText());
            double b = Double.parseDouble(bField.getText());
            double tol = Double.parseDouble(tolField.getText());
            
            Expression expr = new ExpressionBuilder(equationField.getText())
                .variable("x")
                .build()
                .setVariable("π", Math.PI)
                .setVariable("e", Math.E);
            
            double fa = expr.setVariable("x", a).evaluate();
            double fb = expr.setVariable("x", b).evaluate();
            
            double xr = 0;
            double xrPrev = 0;
            double error = 100; // Iniciar con un error grande
            int iter = 0;
            final int MAX_ITER = 1000;
            
            while (error > tol && iter < MAX_ITER) {
                // Guardar valor anterior de xr
                xrPrev = xr;
                
                // Calcular punto medio
                xr = (a + b) / 2;
                
                // Evaluar función en xr
                double fxr = expr.setVariable("x", xr).evaluate();
                
                // Calcular error si no es la primera iteración
                if (iter > 0) {
                    error = Math.abs((xr - xrPrev) / xr) * 100;
                }
                
                // Añadir fila a la tabla de resultados
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(iter + 1));
                row.add(df.format(a));
                row.add(df.format(b));
                row.add(df.format(fa));
                row.add(df.format(fb));
                row.add(df.format(xr));
                row.add(df.format(fxr));
                row.add(iter > 0 ? df.format(error) : "-");
                data.add(row);
                
                // Verificar si se encontró la raíz
                if (Math.abs(fxr) < 1e-10) {
                    break;
                }
                
                // Determinar nuevo intervalo
                double test = fa * fxr;
                if (test < 0) {
                    b = xr;
                    fb = fxr;
                } else if (test > 0) {
                    a = xr;
                    fa = fxr;
                } else {
                    break; // Se encontró la raíz exacta
                }
                
                iter++;
            }
            
            resultsTable.setItems(data);
            
            if (iter < MAX_ITER) {
                resultField.setText(df.format(xr));
            } else {
                resultField.setText("No converge");
                showWarningDialog("Advertencia", "El método no converge en el número máximo de iteraciones.");
            }
            
        } catch (Exception e) {
            showErrorDialog("Error de cálculo", e.getMessage());
        }
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
        try {
            // Obtener los valores de a y b, con manejo mejorado de casos donde no hay cálculo previo
            double a, b;
            try {
                a = Double.parseDouble(aField.getText());
                b = Double.parseDouble(bField.getText());
            } catch (NumberFormatException e) {
                // Valores predeterminados si no hay entrada válida
                a = -10;
                b = 10;
            }
            
            double range = Math.abs(b - a);
            double padding = range * 0.5; // Añadir espacio adicional a cada lado
            
            NumberAxis xAxis = new NumberAxis(a - padding, b + padding, range / 10);
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("x");
            yAxis.setLabel("f(x)");
            
            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Función: f(x) = " + equation);
            
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("f(x) = " + equation);
    
            Expression expr = new ExpressionBuilder(equation)
                .variable("x")
                .build()
                .setVariable("π", Math.PI)
                .setVariable("e", Math.E);
                
            double step = range / 100;
            for (double x = a - padding; x <= b + padding; x += step) {
                try {
                    double y = expr.setVariable("x", x).evaluate();
                    // Limitar los valores de y para que el gráfico sea legible
                    if (y > -1000 && y < 1000) {
                        series.getData().add(new XYChart.Data<>(x, y));
                    }
                } catch (ArithmeticException ex) { /* Ignorar puntos inválidos */ }
            }
            
            chart.getData().add(series);
            
            // Añadir serie para la raíz encontrada, solo si hay un resultado calculado
            if (resultField.getText() != null && !resultField.getText().isEmpty() 
                    && !resultField.getText().equals("No converge")) {
                XYChart.Series<Number, Number> rootSeries = new XYChart.Series<>();
                rootSeries.setName("Raíz");
                try {
                    double root = Double.parseDouble(resultField.getText().replace(",", "."));
                    rootSeries.getData().add(new XYChart.Data<>(root, 0));
                    chart.getData().add(rootSeries);
                    
                    // Estilizar el punto de la raíz
                    rootSeries.getData().get(0).getNode().setStyle("-fx-background-color: red; -fx-background-radius: 5px;");
                } catch (Exception e) {
                    // Si no se puede convertir el valor, ignorar
                }
            }
            
            return chart;
        } catch (Exception e) {
            // Si hay algún error al crear el gráfico, mostrar un gráfico vacío con un mensaje
            NumberAxis xAxis = new NumberAxis(-10, 10, 1);
            NumberAxis yAxis = new NumberAxis();
            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Error al graficar la función: " + e.getMessage());
            return chart;
        }
    }

    private void clearInputs() {
        equationField.clear();
        aField.clear();
        bField.clear();
        tolField.setText("0.0001");
        resultField.clear();
        resultsTable.getItems().clear();
    }

    private void loadExample() {
        equationField.setText("x^3 - 2*x - 5");
        aField.setText("1");
        bField.setText("3");
        tolField.setText("0.0001");
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