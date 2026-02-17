package com.my.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML private BorderPane mainLayout;

    @FXML
    private void showPOS() {
        // Path adjusted to match your project root /fxml/
        loadView("/fxml/POSView.fxml");
    }

    @FXML
    private void showInventory() {
        loadView("/fxml/InventoryView.fxml");
    }

    @FXML
    private void showHistory() {
        // This empty method fixes the 'Error resolving onAction' crash
        System.out.println("Sales History clicked - Feature pending.");
    }

    private void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ ERROR: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainLayout.setCenter(view);

        } catch (IOException e) {
            System.err.println("❌ ERROR: Failed to load " + fxmlPath);
            e.printStackTrace();
        }
    }
}