package com.my.pharmacy.controller;

import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.model.Customer;
import com.my.pharmacy.util.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerController {

    @FXML private TextField nameField, phoneField, cnicField, searchField;
    @FXML private TextArea addressField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName, colType, colPhone, colAddress, colCnic;
    @FXML private Button btnSave;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private Customer editingCustomer = null;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCnic.setCellValueFactory(new PropertyValueFactory<>("cnic"));
    }

    private void loadData() {
        masterData.setAll(customerDAO.getAllCustomers());
        customerTable.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<Customer> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(customer -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return customer.getName().toLowerCase().contains(lower) ||
                        (customer.getPhone() != null && customer.getPhone().contains(newVal)) ||
                        (customer.getCnic()  != null && customer.getCnic().contains(newVal));
            });
        });
        customerTable.setItems(filteredData);
    }

    @FXML
    private void handleEditSelection() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationService.warn("Please select a customer to edit.");
            return;
        }
        editingCustomer = selected;
        nameField.setText(selected.getName());
        phoneField.setText(selected.getPhone());
        addressField.setText(selected.getAddress());
        cnicField.setText(selected.getCnic());
        btnSave.setText("Update Customer");
        btnSave.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;-fx-background-radius: 8;");
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) {
            NotificationService.warn("Customer name is required.");
            return;
        }
        if (editingCustomer == null) {
            Customer newCustomer = new Customer(0, nameField.getText().trim(), phoneField.getText().trim(),
                    addressField.getText().trim(), "REGULAR", 0.0, cnicField.getText().trim());
            customerDAO.addCustomer(newCustomer);
            NotificationService.success("Customer added successfully.");
        } else {
            Customer updatedCustomer = new Customer(editingCustomer.getId(), nameField.getText().trim(),
                    phoneField.getText().trim(), addressField.getText().trim(),
                    editingCustomer.getType(), editingCustomer.getCurrentBalance(), cnicField.getText().trim());
            customerDAO.updateCustomer(updatedCustomer);
            NotificationService.success("Customer updated successfully.");
        }
        loadData();
        clearFields();
    }

    @FXML
    private void clearFields() {
        nameField.clear(); phoneField.clear(); cnicField.clear(); addressField.clear();
        editingCustomer = null;
        btnSave.setText("Save Customer");
        btnSave.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;-fx-background-radius: 8;");
    }
}
