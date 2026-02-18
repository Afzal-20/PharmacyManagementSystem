package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        File configFile = new File("config.properties");

        if (!configFile.exists()) {
            loadSetupWizard();
        } else {
            loadMainApplication();
        }
    }

    private void loadSetupWizard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetupView.fxml"));
            Scene scene = new Scene(loader.load(), 650, 450);
            primaryStage.setTitle("Initial System Setup");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadMainApplication() {
        try {
            // Initialize the database based on the selected mode before loading UI
            DatabaseSetup.initialize();

            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/MainLayout.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1100, 750);

            primaryStage.setTitle("Pharmacy Management System");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}