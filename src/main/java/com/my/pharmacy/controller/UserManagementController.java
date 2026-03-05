package com.my.pharmacy.controller;

import com.my.pharmacy.dao.UserDAO;
import com.my.pharmacy.dao.UserDAOImpl;
import com.my.pharmacy.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserManagementController {

    @FXML private TextField fullNameField, usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colName, colUsername, colRole, colStatus;

    private final UserDAO userDAO = new UserDAOImpl();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("SALESMAN", "ADMIN"));
        setupTable();
        loadUsers();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Disabled"));
    }

    private void loadUsers() {
        userList.setAll(userDAO.getAllUsers());
        userTable.setItems(userList);
    }

    @FXML
    private void handleSaveUser() {
        if (fullNameField.getText().isEmpty() || usernameField.getText().isEmpty() ||
                passwordField.getText().isEmpty() || roleComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Info", "Please fill all fields.");
            return;
        }

        User newUser = new User(0, usernameField.getText(), "", roleComboBox.getValue(), fullNameField.getText(), true);

        if (userDAO.addUser(newUser, passwordField.getText())) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully.");
            clearFields();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Username might already exist.");
        }
    }

    @FXML
    private void handleToggleStatus() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getId() == 1) {
            showAlert(Alert.AlertType.WARNING, "Not Allowed", "You cannot deactivate the primary Super Admin account.");
            return;
        }

        boolean newStatus = !selected.isActive();
        userDAO.toggleUserStatus(selected.getId(), newStatus);
        loadUsers();
    }

    @FXML
    private void clearFields() {
        fullNameField.clear(); usernameField.clear(); passwordField.clear(); roleComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}