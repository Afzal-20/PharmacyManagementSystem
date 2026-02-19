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
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextArea addressField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName, colType, colPhone, colAddress;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("RETAIL", "WHOLESALE"));
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
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
                        customer.getPhone().contains(newVal) ||
                        customer.getType().toLowerCase().contains(lowerCaseFilter);
            });
        });
        customerTable.setItems(filteredData);
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || typeComboBox.getValue() == null) {
            showAlert("Validation Error", "Name and Customer Type are required.");
            return;
        }

        Customer newCustomer = new Customer(
                0,
                nameField.getText(),
                phoneField.getText(),
                addressField.getText(),
                typeComboBox.getValue()
                // Note: If your Customer model has more fields like CNIC, add them here
        );

        customerDAO.addCustomer(newCustomer);
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
        typeComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}