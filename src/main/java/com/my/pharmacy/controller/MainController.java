package com.my.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;

/**
 * Core Navigation Controller for the Pharmacy System.
 * Manages the swapping of views within the main center content area.
 */
public class MainController {

    @FXML private BorderPane mainLayout;

    // --- Outbound / Sales Methods ---

    @FXML
    private void showPOS() {
        // Loads the Point of Sale screen for billing
        loadView("/fxml/POSView.fxml");
    }

    @FXML
    private void showHistory() {
        // Loads the historical sales record and profit reports
        loadView("/fxml/SalesHistory.fxml");
    }

    // --- Inbound / Inventory Methods ---

    @FXML
    private void showInventory() {
        // Loads the detailed batch-wise inventory view
        loadView("/fxml/InventoryView.fxml");
    }

    @FXML
    private void showPurchaseEntry() {
        // Loads the screen to buy stock from Dealers and update batches
        loadView("/fxml/PurchaseEntry.fxml");
    }

    // --- Entity Management Methods (Separated) ---

    @FXML
    private void showCustomers() {
        // Manages Retail and Wholesale buyers
        loadView("/fxml/CustomerManagement.fxml");
    }

    @FXML
    private void showDealers() {
        // Manages Suppliers/Distributors
        loadView("/fxml/DealerManagement.fxml");
    }

    // --- Helper Methods ---

    /**
     * Swaps the center node of the BorderPane with the requested FXML view.
     * @param fxmlPath The path to the FXML file relative to the resources folder.
     */
    private void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ ERROR: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            // Replaces the current center content with the new screen
            mainLayout.setCenter(view);

        } catch (IOException e) {
            System.err.println("❌ ERROR: Failed to load " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Placeholder for logout logic (e.g., returning to Login screen)
        System.out.println("User logged out.");
        System.exit(0);
    }
}