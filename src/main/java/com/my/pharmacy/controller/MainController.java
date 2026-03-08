package com.my.pharmacy.controller;

import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainController {

    public static MainController instance; // Allow other controllers to call navigation methods

    @FXML private BorderPane mainLayout;
    @FXML private Button btnManageUsers; // Added for RBAC enforcement


    @FXML
    public void initialize() {
        instance = this;
        showDashboard(); // Load the dashboard by default

        // Enforce RBAC: Hide User Management from non-admins
        boolean isAdmin = UserSession.getInstance() != null && UserSession.getInstance().getUser().isAdmin();
        if (btnManageUsers != null) {
            btnManageUsers.setVisible(isAdmin);
            btnManageUsers.setManaged(isAdmin);
        }
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
    @FXML public void showExpiry() { loadView("/fxml/ExpiryView.fxml"); }
    @FXML public void showBackup() { loadView("/fxml/BackupView.fxml"); }

    // NEW: Navigation for User Management
    @FXML public void showUserManagement() { loadView("/fxml/UserManagement.fxml"); }

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
    public void showChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChangePasswordDialog.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Change Password");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Clear the session
        UserSession.logout();

        // Go back to the login screen
        if (MainController.instance != null && mainLayout.getScene() != null) {
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            stage.close(); // Close dashboard
            com.my.pharmacy.App.loadLoginScreen(); // Re-open login
        }
    }
}