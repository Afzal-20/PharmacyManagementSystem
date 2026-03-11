package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        log.info("PharmDesk starting up");

        // 1. Ensure all data directories exist
        com.my.pharmacy.util.AppPaths.initialize();

        // 2. Load config from C:\ProgramData\PharmDesk\config.properties
        com.my.pharmacy.util.ConfigUtil.initialize();

        // 3. Initialize Database Schema
        DatabaseSetup.initialize();

        // 4. Register shutdown backup hook
        com.my.pharmacy.util.BackupService.registerShutdownHook();

        // 5. Boot to Login Screen
        loadLoginScreen();
        log.info("PharmDesk startup complete");
    }

    public static void loadLoginScreen() {
        try {
            log.debug("Loading login screen");
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 400, 500);
            primaryStage.setTitle("PharmDesk (LOGIN)");
            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(
                        App.class.getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                log.warn("Logo image not found: {}", e.getMessage());
            }
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            log.error("Critical: Failed to load LoginView.fxml — {}", e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
