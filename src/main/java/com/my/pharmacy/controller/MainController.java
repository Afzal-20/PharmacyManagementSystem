package com.my.pharmacy.controller;

import com.my.pharmacy.util.BackupService;
import com.my.pharmacy.util.NotificationService;
import com.my.pharmacy.util.ShortcutManager;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private static MainController instance;

    /** Returns the active MainController instance. Used by DashboardController for navigation. */
    public static MainController getInstance() { return instance; }

    @FXML private StackPane rootStack;       // Wraps everything — toasts are overlaid here
    @FXML private BorderPane mainLayout;
    @FXML private Button btnManageUsers;

    @FXML
    public void initialize() {
        instance = this;
        log.info("MainController initializing — user: {}",
                UserSession.getInstance() != null ? UserSession.getInstance().getUser().getUsername() : "unknown");

        // Wire the notification toast system to this window's root StackPane
        NotificationService.setContainer(rootStack);

        // Register global keyboard shortcuts (configurable via config.properties)
        rootStack.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) registerShortcuts(newScene);
        });

        showDashboard();

        boolean isAdmin = UserSession.getInstance() != null &&
                UserSession.getInstance().getUser().isAdmin();
        if (btnManageUsers != null) {
            btnManageUsers.setVisible(isAdmin);
            btnManageUsers.setManaged(isAdmin);
        }
        log.info("MainController ready — isAdmin={}", isAdmin);
    }

    private void registerShortcuts(javafx.scene.Scene scene) {
        ShortcutManager.registerAll(scene, new ShortcutManager.MainControllerActions() {
            public void showPOS()        { MainController.this.showPOS(); }
            public void showInventory()  { MainController.this.showInventory(); }
            public void showPurchase()   { MainController.this.showPurchaseEntry(); }
            public void showHistory()    { MainController.this.showHistory(); }
            public void showKhata()      { MainController.this.showKhata(); }
            public void showCustomers()  { MainController.this.showCustomers(); }
            public void showDealers()    { MainController.this.showDealers(); }
            public void showExpiry()     { MainController.this.showExpiry(); }
            public void showDashboard()  { MainController.this.showDashboard(); }
        });

        // Global backup fallback — fires from any screen when the Backup screen is not active.
        // When the Backup screen IS active, BackupController.setBackupNowAction() takes priority
        // and also refreshes the backup list after creating the backup.
        ShortcutManager.setGlobalBackupFallback(() -> {
            java.io.File result = BackupService.createBackup();
            if (result != null) NotificationService.success("Backup created: " + result.getName());
            else NotificationService.error("Backup failed. Check that the database exists.");
        });

        log.info("All shortcuts registered");
    }

    @FXML public void showDashboard()      { loadView("/fxml/DashboardView.fxml"); }
    @FXML public void showPOS()            { loadView("/fxml/POSView.fxml"); }
    @FXML public void showHistory()        { loadView("/fxml/SalesHistory.fxml"); }
    @FXML public void showInventory()      { loadView("/fxml/InventoryView.fxml"); }
    @FXML public void showPurchaseEntry()  { loadView("/fxml/PurchaseEntry.fxml"); }
    @FXML public void showCustomers()      { loadView("/fxml/CustomerManagement.fxml"); }
    @FXML public void showDealers()        { loadView("/fxml/DealerManagement.fxml"); }
    @FXML public void showItemLedger()     { loadView("/fxml/ItemLedger.fxml"); }
    @FXML public void showKhata()          { loadView("/fxml/KhataManagement.fxml"); }
    @FXML public void showExpiry()         { loadView("/fxml/ExpiryView.fxml"); }
    @FXML public void showBackup()         { loadView("/fxml/BackupView.fxml"); }
    @FXML public void showUserManagement() { loadView("/fxml/UserManagement.fxml"); }

    private void loadView(String fxmlPath) {
        log.debug("Loading view: {}", fxmlPath);
        // Clear screen-specific shortcuts before loading new screen
        ShortcutManager.clearScreenActions();
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                log.error("FXML not found: {}", fxmlPath);
                NotificationService.error("Could not load screen: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainLayout.setCenter(view);
            log.debug("View loaded: {}", fxmlPath);
        } catch (IOException e) {
            log.error("Failed to load view {}: {}", fxmlPath, e.getMessage(), e);
            NotificationService.error("Failed to load screen.");
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
            log.info("Change password dialog closed");
        } catch (IOException e) {
            log.error("Failed to open ChangePasswordDialog: {}", e.getMessage(), e);
            NotificationService.error("Could not open password change dialog.");
        }
    }

    @FXML
    private void handleLogout() {
        log.info("User logging out: {}", UserSession.getInstance().getUser().getUsername());
        UserSession.logout();
        if (mainLayout.getScene() != null) {
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            stage.close();
            com.my.pharmacy.App.loadLoginScreen();
        }
    }
}