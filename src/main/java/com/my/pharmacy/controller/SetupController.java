package com.my.pharmacy.controller;

import com.my.pharmacy.App;
import javafx.fxml.FXML;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SetupController {

    @FXML
    private void handleRetailSelection() {
        saveConfig("RETAIL");
    }

    @FXML
    private void handleWholesaleSelection() {
        saveConfig("WHOLESALE");
    }

    private void saveConfig(String mode) {
        Properties props = new Properties();
        props.setProperty("app.mode", mode);
        props.setProperty("db.name", mode.toLowerCase() + "_pharmacy.db");

        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, "Pharmacy System Configuration");
            // Switch to the main application view
            App.loadMainApplication();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}