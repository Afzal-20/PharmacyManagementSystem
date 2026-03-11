package com.my.pharmacy.controller;

import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.model.Customer;
import com.my.pharmacy.util.NotificationService;
import javafx.fxml.FXML;
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
            NotificationService.warn("Customer name is required.");
            return;
        }
        Customer customer = new Customer(0, nameField.getText().trim(), phoneField.getText().trim(),
                addressField.getText().trim(), "REGULAR", 0.0, "");
        int newId = customerDAO.addCustomer(customer);
        if (newId != -1) {
            this.savedCustomer = customerDAO.getCustomerById(newId);
        } else {
            NotificationService.error("Failed to save customer. Please try again.");
            return;
        }
        closeWindow();
    }

    public Customer getSavedCustomer() { return savedCustomer; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}
