package com.my.pharmacy.controller;

import com.my.pharmacy.dao.UserDAO;
import com.my.pharmacy.dao.UserDAOImpl;
import com.my.pharmacy.model.User;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // Allow pressing "Enter" to login
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User authenticatedUser = userDAO.authenticate(user, pass);

        if (authenticatedUser != null) {
            // 1. Set Session
            UserSession.login(authenticatedUser);
            System.out.println("✅ Login Successful: " + authenticatedUser.getUsername() + " (" + authenticatedUser.getRole() + ")");

            // 2. Load Main Dashboard
            loadMainDashboard();
        } else {
            errorLabel.setText("Invalid credentials. Please try again.");
        }
    }

    private void loadMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Pharmacy Management System - Logged in as: " + UserSession.getInstance().getUser().getUsername());
            stage.setScene(new Scene(root, 1100, 750));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Critical Error: Failed to load dashboard.");
        }
    }
}