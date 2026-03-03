package com.my.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;

public class MainController {

    public static MainController instance; // Allow other controllers to call navigation methods

    @FXML private BorderPane mainLayout;

    @FXML
    public void initialize() {
        instance = this;
        showDashboard(); // Load the dashboard by default
    }

    @FXML public void showDashboard() { loadView("/fxml/DashboardView.fxml"); }
    @FXML public void showPOS() { loadView("/fxml/POSView.fxml"); }
    @FXML public void showHistory() { loadView("/fxml/SalesHistory.fxml"); }
    @FXML public void showInventory() { loadView("/fxml/InventoryView.fxml"); }
    @FXML public void showPurchaseEntry() { loadView("/fxml/PurchaseEntry.fxml"); }
    @FXML public void showCustomers() { loadView("/fxml/CustomerManagement.fxml"); }
    @FXML public void showDealers() { loadView("/fxml/DealerManagement.fxml"); }
    @FXML public void showItemLedger() { loadView("/fxml/ItemLedger.fxml"); }
    @FXML public void showKhata() { loadView("/fxml/KhataManagement.fxml"); }

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

    @FXML
    private void handleLogout() {
        System.exit(0);
    }
}