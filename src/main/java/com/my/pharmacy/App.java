package com.my.pharmacy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App Entry Point
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // 1. Point to your specific FXML location
            // Note: The path must start with "/" and match your resources folder structure
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));

            // 2. Load the FXML file
            Parent root = fxmlLoader.load();

            // 3. Create the Scene (Window content)
            // We set a default size of 1000x700 for a comfortable desktop view
            Scene scene = new Scene(root, 1000, 700);

            // 4. Configure the Window (Stage)
            stage.setTitle("Pharmacy Management System");
            stage.setScene(scene);

            // Optional: Start maximized?
            // stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {
            System.err.println("❌ CRITICAL ERROR: Could not load MainLayout.fxml");
            System.err.println("Please check that 'MainLayout.fxml' is inside 'src/main/resources/fxml/'");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        com.my.pharmacy.config.DatabaseSetup.initialize();
        launch();
    }
}