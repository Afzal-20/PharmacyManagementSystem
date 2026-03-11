package com.my.pharmacy.controller;

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

    public static MainController instance;

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
        // Navigation shortcuts
        ShortcutManager.register(scene, "shortcut.pos",           "F1",  this::showPOS);
        ShortcutManager.register(scene, "shortcut.inventory",     "F2",  this::showInventory);
        ShortcutManager.register(scene, "shortcut.purchase",      "F3",  this::showPurchaseEntry);
        ShortcutManager.register(scene, "shortcut.sales_history", "F4",  this::showHistory);
        ShortcutManager.register(scene, "shortcut.khata",         "F5",  this::showKhata);
        ShortcutManager.register(scene, "shortcut.customers",     "F6",  this::showCustomers);
        ShortcutManager.register(scene, "shortcut.dealers",       "F7",  this::showDealers);
        ShortcutManager.register(scene, "shortcut.expiry",        "F8",  this::showExpiry);
        ShortcutManager.register(scene, "shortcut.dashboard",     "F9",  this::showDashboard);
        log.info("Navigation shortcuts registered");
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
