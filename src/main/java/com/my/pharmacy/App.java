package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // 1. Initialize Database Schema
        DatabaseSetup.initialize();

        // 2. Boot directly to Login Screen
        loadLoginScreen();
    }

    public static void loadLoginScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 400, 500);

            primaryStage.setTitle("Login - Pharmacy System");
            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                System.err.println("Warning: Logo image not found at /images/logo.png");
            }
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("❌ Critical Error: Failed to load LoginView.fxml");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}