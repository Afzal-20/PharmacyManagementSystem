package com.my.pharmacy.controller;

import com.my.pharmacy.App;
import com.my.pharmacy.dao.UserDAO;
import com.my.pharmacy.dao.UserDAOImpl;
import com.my.pharmacy.model.User;
import com.my.pharmacy.util.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

import static com.my.pharmacy.App.primaryStage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAOImpl();

    // Brute-force protection state
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS  = 5;
    private static final int LOCKOUT_MS    = 5000; // 5 seconds per lockout
    private boolean isLockedOut = false;

    @FXML
    public void initialize() {
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        if (isLockedOut) return; // Ignore clicks during lockout

        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User authenticatedUser = userDAO.authenticate(user, pass);

        if (authenticatedUser != null) {
            failedAttempts = 0; // Reset on success
            UserSession.login(authenticatedUser);
            System.out.println("✅ Login Successful: (" + authenticatedUser.getRole() + ")");
            loadMainDashboard();
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;

            if (failedAttempts >= MAX_ATTEMPTS) {
                triggerLockout();
            } else {
                errorLabel.setText("Invalid credentials. " + remaining + " attempt(s) remaining.");
            }
        }
    }

    private void triggerLockout() {
        isLockedOut = true;
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        errorLabel.setStyle("-fx-text-fill: red;");

        // Countdown on a background thread, UI updates on FX thread
        Thread lockoutThread = new Thread(() -> {
            try {
                for (int i = LOCKOUT_MS / 1000; i > 0; i--) {
                    final int secondsLeft = i;
                    Platform.runLater(() ->
                            errorLabel.setText("Too many failed attempts. Try again in " + secondsLeft + "s..."));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                Platform.runLater(() -> {
                    failedAttempts = 0;
                    isLockedOut = false;
                    usernameField.setDisable(false);
                    passwordField.setDisable(false);
                    passwordField.clear();
                    errorLabel.setStyle("");
                    errorLabel.setText("Account unlocked. Please try again.");
                });
            }
        });
        lockoutThread.setDaemon(true);
        lockoutThread.start();
    }

    private void loadMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("PharmDesk (" + UserSession.getInstance().getUser().getRole() + ")");

            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                System.err.println("Warning: Logo image not found at /images/logo.png");
            }

            stage.setScene(new Scene(root, 1100, 750));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Critical Error: Failed to load dashboard.");
        }
    }
}