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

    // --- Customer UI ---
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private TableView<LedgerRecord> customerLedgerTable;
    @FXML private TableColumn<LedgerRecord, String> colCustDate, colCustDesc;
    @FXML private TableColumn<LedgerRecord, Double> colCustDebit, colCustCredit;
    @FXML private Label lblCustomerBalance;
    @FXML private TextField custPaymentAmountField, custPaymentDescField;

    // --- Dealer UI ---
    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private TableView<LedgerRecord> dealerLedgerTable;
    @FXML private TableColumn<LedgerRecord, String> colDealDate, colDealDesc;
    @FXML private TableColumn<LedgerRecord, Double> colDealDebit, colDealCredit;
    @FXML private Label lblDealerBalance;
    @FXML private TextField dealPaymentAmountField, dealPaymentDescField;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    @FXML
    public void initialize() {
        setupCustomerUI();
        setupDealerUI();
    }

    private void setupCustomerUI() {
        colCustDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCustDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCustCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));

        customerComboBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) { return c == null ? "" : c.getName(); }
            @Override public Customer fromString(String s) { return null; }
        });
    }

    private void setupDealerUI() {
        colDealDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDealDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDealDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colDealCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));

        dealerComboBox.setItems(FXCollections.observableArrayList(dealerDAO.getAllDealers()));
        dealerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Dealer d) { return d == null ? "" : d.getCompanyName() + " (" + d.getName() + ")"; }
            @Override public Dealer fromString(String s) { return null; }
        });
    }

    // --- CUSTOMER LOGIC ---

    @FXML
    private void loadCustomerLedger() {
        Customer c = customerComboBox.getValue();
        if (c == null) return;

        Customer refreshed = customerDAO.getCustomerById(c.getId());
        lblCustomerBalance.setText(String.format("Rs. %.2f", refreshed.getCurrentBalance()));
        customerLedgerTable.setItems(FXCollections.observableArrayList(paymentDAO.getCustomerLedger(c.getId())));
    }

    @FXML
    private void handleSaveCustomerPayment() {
        Customer c = customerComboBox.getValue();
        if (c == null) { showAlert("Error", "Please select a customer first."); return; }

        try {
            double amount = Double.parseDouble(custPaymentAmountField.getText());
            String desc = custPaymentDescField.getText().isEmpty() ? "Cash Payment Received" : custPaymentDescField.getText();

            Payment p = new Payment(0, c.getId(), "CUSTOMER", amount, "CASH", desc, new Timestamp(System.currentTimeMillis()));
            paymentDAO.recordPayment(p);

            showAlert("Success", "Customer payment recorded successfully.");
            custPaymentAmountField.clear(); custPaymentDescField.clear();
            loadCustomerLedger();
        } catch (NumberFormatException e) { showAlert("Invalid Input", "Please enter a valid amount."); }
    }

    // --- DEALER LOGIC ---

    @FXML
    private void loadDealerLedger() {
        Dealer d = dealerComboBox.getValue();
        if (d == null) return;

        Dealer refreshed = dealerDAO.getDealerById(d.getId()); // Make sure Dealer model has currentBalance mapping in DAO if you want it exact, otherwise calculate from Ledger or let PaymentDAO update it.
        // Wait, DealerDAO needs to fetch current_balance. If not mapped in Dealer model yet, we will fetch it manually or map it.

        // Let's assume Dealer model in DealerDAOImpl has it, or we rely on the DB updating. For absolute safety, let's load the ledger rows.
        dealerLedgerTable.setItems(FXCollections.observableArrayList(paymentDAO.getDealerLedger(d.getId())));

        // Calculate balance dynamically from Ledger if Dealer model isn't updated with currentBalance
        double dynamicBalance = dealerLedgerTable.getItems().stream().mapToDouble(r -> r.getDebit() - r.getCredit()).sum();
        lblDealerBalance.setText(String.format("Rs. %.2f", dynamicBalance));
    }

    @FXML
    private void handleSaveDealerPayment() {
        Dealer d = dealerComboBox.getValue();
        if (d == null) { showAlert("Error", "Please select a dealer first."); return; }

        try {
            double amount = Double.parseDouble(dealPaymentAmountField.getText());
            String desc = dealPaymentDescField.getText().isEmpty() ? "Cash/Bank Payment Made" : dealPaymentDescField.getText();

            Payment p = new Payment(0, d.getId(), "DEALER", amount, "CASH", desc, new Timestamp(System.currentTimeMillis()));
            paymentDAO.recordPayment(p);

            showAlert("Success", "Dealer payment recorded successfully.");
            dealPaymentAmountField.clear(); dealPaymentDescField.clear();
            loadDealerLedger();
        } catch (NumberFormatException e) { showAlert("Invalid Input", "Please enter a valid amount."); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}