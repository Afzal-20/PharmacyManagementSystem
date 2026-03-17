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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.my.pharmacy.App.primaryStage;

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAOImpl();

    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MS   = 5000;
    private boolean isLockedOut = false;

    @FXML
    public void initialize() {
        passwordField.setOnAction(e -> handleLogin());
        log.debug("LoginController initialized");
    }

    @FXML
    private void handleLogin() {
        if (isLockedOut) return;

        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        log.info("Login attempt: username={}", user);
        User authenticatedUser = userDAO.authenticate(user, pass);

        if (authenticatedUser != null) {
            failedAttempts = 0;
            UserSession.login(authenticatedUser);
            log.info("Login successful: user={} role={}", authenticatedUser.getUsername(), authenticatedUser.getRole());
            loadMainDashboard();
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;
            log.warn("Login failed for user={} — {} attempt(s) remaining", user, remaining);

            if (failedAttempts >= MAX_ATTEMPTS) {
                log.warn("Max login attempts reached for user={} — triggering lockout", user);
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
        errorLabel.getStyleClass().add("error-label-lockout");

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
                    errorLabel.getStyleClass().remove("error-label-lockout");
                    errorLabel.setText("Account unlocked. Please try again.");
                    log.info("Login lockout expired — fields re-enabled");
                });
            }
        });
        lockoutThread.setDaemon(true);
        lockoutThread.start();
    }

    private void loadMainDashboard() {
        try {
            log.debug("Loading main dashboard");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("PharmDesk (" + UserSession.getInstance().getUser().getRole() + ")");
            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(
                        App.class.getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                log.warn("Logo image not found: {}", e.getMessage());
            }
            stage.setScene(new Scene(root, 1100, 750));
            stage.centerOnScreen();
            stage.show();
            log.info("Dashboard loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load dashboard: {}", e.getMessage(), e);
            errorLabel.setText("Critical Error: Failed to load dashboard.");
        }
    }
}