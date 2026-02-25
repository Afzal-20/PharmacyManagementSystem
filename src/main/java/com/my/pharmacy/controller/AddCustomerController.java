package com.my.pharmacy.controller;

import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCustomerController {

    @FXML private TextField nameField, phoneField;
    @FXML private TextArea addressField;

    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private Customer savedCustomer;

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty()) return;

        // FIXED: Added 0.0 (balance) and null (area code/name)
        Customer customer = new Customer(0, nameField.getText(), phoneField.getText(),
                addressField.getText(), "REGULAR", 0.0, null, null, "");
        customerDAO.addCustomer(customer);

        this.savedCustomer = customerDAO.getAllCustomers().stream()
                .filter(c -> c.getName().equals(customer.getName()))
                .findFirst().orElse(null);

        closeWindow();
    }

    public Customer getSavedCustomer() { return savedCustomer; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}