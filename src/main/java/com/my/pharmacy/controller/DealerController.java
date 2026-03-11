package com.my.pharmacy.controller;

import com.my.pharmacy.dao.DealerDAO;
import com.my.pharmacy.dao.DealerDAOImpl;
import com.my.pharmacy.model.Dealer;
import com.my.pharmacy.util.DialogUtil;
import com.my.pharmacy.util.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class DealerController {

    @FXML private TextField nameField, companyField, phoneField, licenseField, searchField;
    @FXML private TextArea addressField;
    @FXML private TableView<Dealer> dealerTable;
    @FXML private TableColumn<Dealer, String> colCompany, colName, colPhone, colLicense;
    @FXML private Button btnSave;

    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final ObservableList<Dealer> masterData = FXCollections.observableArrayList();
    private Dealer editingDealer = null;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNo"));
    }

    private void loadData() {
        masterData.setAll(dealerDAO.getAllDealers());
        dealerTable.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<Dealer> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(dealer -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (dealer.getCompanyName() != null && dealer.getCompanyName().toLowerCase().contains(lower)) ||
                        (dealer.getLicenseNo() != null && dealer.getLicenseNo().toLowerCase().contains(lower)) ||
                        (dealer.getName()      != null && dealer.getName().toLowerCase().contains(lower));
            });
        });
        dealerTable.setItems(filteredData);
    }

    @FXML
    private void handleEditSelection() {
        Dealer selected = dealerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationService.warn("Please select a dealer to edit.");
            return;
        }
        editingDealer = selected;
        nameField.setText(selected.getName());
        companyField.setText(selected.getCompanyName());
        phoneField.setText(selected.getPhone());
        addressField.setText(selected.getAddress());
        licenseField.setText(selected.getLicenseNo());
        btnSave.setText("Update Dealer");
        btnSave.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    private void handleDeleteSelection() {
        Dealer selected = dealerTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        com.my.pharmacy.dao.PaymentDAO paymentDAO = new com.my.pharmacy.dao.PaymentDAOImpl();
        if (!paymentDAO.getDealerLedger(selected.getId()).isEmpty()) {
            NotificationService.error("Cannot delete dealer — they have existing payment/ledger history.");
            return;
        }

        if (DialogUtil.confirm("Delete Dealer", "Delete " + selected.getCompanyName() + "?", "This cannot be undone.")) {
            dealerDAO.deleteDealer(selected.getId());
            loadData();
            clearFields();
        }
    }

    @FXML
    private void handleSave() {
        if (companyField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
            NotificationService.warn("Company Name and Phone are required.");
            return;
        }
        if (editingDealer == null) {
            Dealer newDealer = new Dealer(0, nameField.getText().trim(), companyField.getText().trim(),
                    phoneField.getText().trim(), addressField.getText().trim(), licenseField.getText().trim());
            dealerDAO.addDealer(newDealer);
            NotificationService.success("Dealer registered successfully.");
        } else {
            Dealer updatedDealer = new Dealer(editingDealer.getId(), nameField.getText().trim(), companyField.getText().trim(),
                    phoneField.getText().trim(), addressField.getText().trim(), licenseField.getText().trim());
            dealerDAO.updateDealer(updatedDealer);
            NotificationService.success("Dealer updated successfully.");
        }
        loadData();
        clearFields();
    }

    @FXML
    private void clearFields() {
        nameField.clear(); companyField.clear(); phoneField.clear(); licenseField.clear(); addressField.clear();
        editingDealer = null;
        btnSave.setText("Save Dealer");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
    }
}
