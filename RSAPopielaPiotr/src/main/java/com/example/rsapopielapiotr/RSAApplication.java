package com.example.rsapopielapiotr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RSAApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RSAApplication.class.getResource("app-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 620);
        stage.setScene(scene);
        stage.setTitle("RSA Piotr Popiela");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}