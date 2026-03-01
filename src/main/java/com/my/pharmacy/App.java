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
        loadMainApplication();
    }

    public static void loadMainApplication() {
        try {
            // Initialize the database structure for the wholesale environment
            DatabaseSetup.initialize();

            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/MainLayout.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1100, 750);

            primaryStage.setTitle("Pharmacy Management System (Wholesale)");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("❌ Critical Error: Failed to load MainLayout.fxml");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}