package com.my.pharmacy.controller;

import com.my.pharmacy.dao.UserDAO;
import com.my.pharmacy.dao.UserDAOImpl;
import com.my.pharmacy.model.User;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField currentPassHidden, newPassHidden, confirmPassHidden;
    @FXML private TextField currentPassText, newPassText, confirmPassText;
    @FXML private CheckBox showPasswordCheckBox;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // Bind the text properties so typing in one updates the other
        currentPassText.textProperty().bindBidirectional(currentPassHidden.textProperty());
        newPassText.textProperty().bindBidirectional(newPassHidden.textProperty());
        confirmPassText.textProperty().bindBidirectional(confirmPassHidden.textProperty());

        // Toggle visibility based on the checkbox
        currentPassText.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        currentPassHidden.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());

        newPassText.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        newPassHidden.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());

        confirmPassText.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        confirmPassHidden.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
    }

    @FXML
    private void handleSave() {
        String currentPass = currentPassHidden.getText();
        String newPass = newPassHidden.getText();
        String confirmPass = confirmPassHidden.getText();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill in all password fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Mismatch", "The new passwords do not match.");
            return;
        }

        User currentUser = UserSession.getInstance().getUser();

        // Authenticate to verify the current password is correct
        User verifiedUser = userDAO.authenticate(currentUser.getUsername(), currentPass);

        if (verifiedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Authentication Failed", "The current password you entered is incorrect.");
            return;
        }

        // Proceed to update the password via BCrypt
        if (userDAO.updatePassword(currentUser.getId(), newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been changed successfully.");
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "System Error", "Failed to update password. Please contact support.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) currentPassHidden.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}