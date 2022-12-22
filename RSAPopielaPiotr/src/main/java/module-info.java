module com.example.rsapopielapiotr {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.rsapopielapiotr to javafx.fxml;
    exports com.example.rsapopielapiotr;
}