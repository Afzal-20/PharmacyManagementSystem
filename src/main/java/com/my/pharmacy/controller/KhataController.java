package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.sql.Timestamp;

public class KhataController {

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private TableView<LedgerRecord> ledgerTable;
    @FXML private TableColumn<LedgerRecord, String> colDate, colDesc;
    @FXML private TableColumn<LedgerRecord, Double> colDebit, colCredit;
    @FXML private Label lblBalance;
    @FXML private TextField paymentAmountField, paymentDescField;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    @FXML
    public void initialize() {
        setupColumns();
        setupCustomerDropdown();
    }

    private void setupColumns() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
    }

    private void setupCustomerDropdown() {
        customerComboBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) { return c == null ? "" : c.getName(); }
            @Override public Customer fromString(String s) { return null; }
        });
    }

    @FXML
    private void loadLedger() {
        Customer c = customerComboBox.getValue();
        if (c == null) return;

        // Refresh customer from DB to get latest balance
        Customer refreshedCustomer = customerDAO.getCustomerById(c.getId());
        lblBalance.setText(String.format("Rs. %.2f", refreshedCustomer.getCurrentBalance()));

        ledgerTable.setItems(FXCollections.observableArrayList(paymentDAO.getCustomerLedger(c.getId())));
    }

    @FXML
    private void handleSavePayment() {
        Customer c = customerComboBox.getValue();
        if (c == null) {
            showAlert("Error", "Please select a customer first.");
            return;
        }
        try {
            double amount = Double.parseDouble(paymentAmountField.getText());
            String desc = paymentDescField.getText().isEmpty() ? "Cash Payment" : paymentDescField.getText();

            Payment p = new Payment(0, c.getId(), "CUSTOMER", amount, "CASH", desc, new Timestamp(System.currentTimeMillis()));
            paymentDAO.recordPayment(p);

            showAlert("Success", "Payment recorded successfully.");
            paymentAmountField.clear();
            paymentDescField.clear();
            loadLedger(); // Refresh table and balance

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid amount.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}