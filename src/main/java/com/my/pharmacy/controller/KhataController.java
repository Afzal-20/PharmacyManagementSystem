package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import java.sql.Timestamp;

public class KhataController {

    // --- Customer UI ---
    @FXML private TextField searchCustomerField;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private TableView<LedgerRecord> customerLedgerTable;
    @FXML private TableColumn<LedgerRecord, String> colCustDate, colCustDesc;
    @FXML private TableColumn<LedgerRecord, Double> colCustDebit, colCustCredit;
    @FXML private Label lblCustomerBalance;
    @FXML private TextField custPaymentAmountField, custPaymentDescField;
    @FXML private VBox vboxCustPayment;

    // --- Dealer UI ---
    @FXML private TextField searchDealerField;
    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private TableView<LedgerRecord> dealerLedgerTable;
    @FXML private TableColumn<LedgerRecord, String> colDealDate, colDealDesc;
    @FXML private TableColumn<LedgerRecord, Double> colDealDebit, colDealCredit;
    @FXML private Label lblDealerBalance;
    @FXML private TextField dealPaymentAmountField, dealPaymentDescField;
    @FXML private VBox vboxDealPayment;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    private final ObservableList<Customer> customerMasterData = FXCollections.observableArrayList();
    private final ObservableList<Dealer> dealerMasterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCustomerUI();
        setupDealerUI();

        boolean isAdmin = com.my.pharmacy.util.UserSession.getInstance() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser().isAdmin();
        vboxCustPayment.setVisible(isAdmin); vboxCustPayment.setManaged(isAdmin);
        vboxDealPayment.setVisible(isAdmin); vboxDealPayment.setManaged(isAdmin);
    }

    private void setupCustomerUI() {
        colCustDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCustDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCustCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));

        customerMasterData.setAll(customerDAO.getAllCustomers());
        FilteredList<Customer> filteredCustomers = new FilteredList<>(customerMasterData, p -> true);

        searchCustomerField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredCustomers.setPredicate(c -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return c.getName().toLowerCase().contains(newVal.toLowerCase());
            });
            if (!filteredCustomers.isEmpty()) customerComboBox.getSelectionModel().selectFirst();
        });

        customerComboBox.setItems(filteredCustomers);
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

        dealerMasterData.setAll(dealerDAO.getAllDealers());
        FilteredList<Dealer> filteredDealers = new FilteredList<>(dealerMasterData, p -> true);

        searchDealerField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredDealers.setPredicate(d -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (d.getCompanyName() != null && d.getCompanyName().toLowerCase().contains(lower)) ||
                        (d.getName() != null && d.getName().toLowerCase().contains(lower));
            });
            if (!filteredDealers.isEmpty()) dealerComboBox.getSelectionModel().selectFirst();
        });

        dealerComboBox.setItems(filteredDealers);
        dealerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Dealer d) { return d == null ? "" : d.getCompanyName() + " (" + d.getName() + ")"; }
            @Override public Dealer fromString(String s) { return null; }
        });
    }

    @FXML
    private void loadCustomerLedger() {
        Customer c = customerComboBox.getValue();
        if (c == null) return;
        lblCustomerBalance.setText(String.format("Rs. %,.2f", paymentDAO.getDynamicCustomerBalance(c.getId())));
        customerLedgerTable.setItems(FXCollections.observableArrayList(paymentDAO.getCustomerLedger(c.getId())));
    }

    @FXML
    private void handleSaveCustomerPayment() {
        Customer c = customerComboBox.getValue();
        if (c == null) { showAlert("Error", "Please select a customer first."); return; }
        try {
            double amount = Double.parseDouble(custPaymentAmountField.getText());
            String desc = custPaymentDescField.getText().isEmpty() ? "Cash Payment Received" : custPaymentDescField.getText();
            paymentDAO.recordPayment(new Payment(0, c.getId(), "CUSTOMER", amount, "CASH", desc, new Timestamp(System.currentTimeMillis())));
            showAlert("Success", "Customer payment recorded successfully.");
            custPaymentAmountField.clear(); custPaymentDescField.clear();
            loadCustomerLedger();
        } catch (NumberFormatException e) { showAlert("Invalid Input", "Please enter a valid amount."); }
    }

    @FXML
    private void loadDealerLedger() {
        Dealer d = dealerComboBox.getValue();
        if (d == null) return;
        lblDealerBalance.setText(String.format("Rs. %,.2f", paymentDAO.getDynamicDealerBalance(d.getId())));
        dealerLedgerTable.setItems(FXCollections.observableArrayList(paymentDAO.getDealerLedger(d.getId())));
    }

    @FXML
    private void handleSaveDealerPayment() {
        Dealer d = dealerComboBox.getValue();
        if (d == null) { showAlert("Error", "Please select a dealer first."); return; }
        try {
            double amount = Double.parseDouble(dealPaymentAmountField.getText());
            String desc = dealPaymentDescField.getText().isEmpty() ? "Cash/Bank Payment Made" : dealPaymentDescField.getText();
            paymentDAO.recordPayment(new Payment(0, d.getId(), "DEALER", amount, "CASH", desc, new Timestamp(System.currentTimeMillis())));
            showAlert("Success", "Dealer payment recorded successfully.");
            dealPaymentAmountField.clear(); dealPaymentDescField.clear();
            loadDealerLedger();
        } catch (NumberFormatException e) { showAlert("Invalid Input", "Please enter a valid amount."); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}