module com.example.aespopielapiotr {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.aespopielapiotr to javafx.fxml;
    exports com.example.aespopielapiotr;
}