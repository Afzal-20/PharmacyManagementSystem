package com.my.pharmacy.controller;

import com.my.pharmacy.dao.UserDAO;
import com.my.pharmacy.dao.UserDAOImpl;
import com.my.pharmacy.model.User;
import com.my.pharmacy.util.NotificationService;
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
        currentPassText.textProperty().bindBidirectional(currentPassHidden.textProperty());
        newPassText.textProperty().bindBidirectional(newPassHidden.textProperty());
        confirmPassText.textProperty().bindBidirectional(confirmPassHidden.textProperty());

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
        String newPass     = newPassHidden.getText();
        String confirmPass = confirmPassHidden.getText();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            NotificationService.warn("Please fill in all password fields.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            NotificationService.error("New passwords do not match.");
            return;
        }

        User currentUser   = UserSession.getInstance().getUser();
        User verifiedUser  = userDAO.authenticate(currentUser.getUsername(), currentPass);

        if (verifiedUser == null) {
            NotificationService.error("Current password is incorrect.");
            return;
        }

        if (userDAO.updatePassword(currentUser.getId(), newPass)) {
            NotificationService.success("Password changed successfully.");
            closeWindow();
        } else {
            NotificationService.error("Failed to update password. Please contact support.");
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) currentPassHidden.getScene().getWindow()).close(); }
}
