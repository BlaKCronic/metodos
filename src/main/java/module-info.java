module com.example.numericalc {
    requires javafx.controls;
    requires javafx.fxml;
    requires exp4j;
    requires javafx.graphics;
    
    opens com.example to javafx.graphics, javafx.fxml;
    opens com.example.controllers to javafx.fxml;
}