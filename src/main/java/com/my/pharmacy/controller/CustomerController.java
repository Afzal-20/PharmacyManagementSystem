package com.my.pharmacy.controller;

import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.model.Customer;
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
    @FXML private TableColumn<Customer, String> colName, colType, colPhone, colAddress;
    @FXML private TableColumn<Customer, String> colCnic;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();

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
                String lowerCaseFilter = newVal.toLowerCase();

                return customer.getName().toLowerCase().contains(lowerCaseFilter) ||
                        (customer.getPhone() != null && customer.getPhone().contains(newVal)) ||
                        (customer.getCnic() != null && customer.getCnic().contains(newVal));
            });
        });
        customerTable.setItems(filteredData);
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Name is required.");
            return;
        }

        Customer newCustomer = new Customer(
                0,
                nameField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim(),
                "REGULAR",
                0.0,
                null,
                null,
                cnicField.getText().trim()
        );

        // FIX #4 side effect: addCustomer() now returns int — ignore the return value here
        // since CustomerController just needs the table to refresh.
        int newId = customerDAO.addCustomer(newCustomer);
        if (newId == -1) {
            showAlert("Database Error", "Failed to save customer. Please try again.");
            return;
        }

        loadData();
        clearFields();
        showAlert("Success", "Customer added successfully!");
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        phoneField.clear();
        cnicField.clear();
        addressField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}