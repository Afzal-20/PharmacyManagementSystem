package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // 1. Initialize Database
        DatabaseSetup.initialize();

        // 2. Load Login Screen (Entry Point)
        loadLoginScreen();
    }

    public static void loadLoginScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 400, 550); // Smaller size for login

            primaryStage.setTitle("Login - Pharmacy System");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("❌ Critical Error: Failed to load LoginView.fxml");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}