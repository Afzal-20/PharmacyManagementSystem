package com.my.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    public void showPOS() {
        try {
            // 1. Load the POS View
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/POSView.fxml"));
            javafx.scene.Parent posView = loader.load();

            // 2. Set it into the CENTER of the Main Layout
            mainContainer.setCenter(posView);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showInventory() {
        System.out.println("Navigating to Inventory Screen...");
        // TODO: Load Inventory View
    }

    @FXML
    public void showHistory() {
        System.out.println("Navigating to History Screen...");
    }
}