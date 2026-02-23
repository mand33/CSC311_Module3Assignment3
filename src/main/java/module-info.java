module org.example.csc311_module3assignment3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.csc311_module3assignment3 to javafx.fxml;
    exports org.example.csc311_module3assignment3;
}