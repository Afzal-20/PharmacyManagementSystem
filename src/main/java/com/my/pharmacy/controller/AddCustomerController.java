package com.my.pharmacy.controller;

import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCustomerController {
    @FXML private TextField nameField, phoneField;
    @FXML private TextArea addressField;
    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private Customer savedCustomer;

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Customer name is required.");
            return;
        }

        // Updated Constructor call
        Customer customer = new Customer(0, nameField.getText().trim(), phoneField.getText().trim(),
                addressField.getText().trim(), "REGULAR", 0.0, "");

        int newId = customerDAO.addCustomer(customer);
        if (newId != -1) {
            this.savedCustomer = customerDAO.getCustomerById(newId);
        } else {
            showAlert("Database Error", "Failed to save customer. Please try again.");
            return;
        }
        closeWindow();
    }

    public Customer getSavedCustomer() { return savedCustomer; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}