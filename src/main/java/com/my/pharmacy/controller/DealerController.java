package com.my.pharmacy.controller;

import com.my.pharmacy.dao.DealerDAO;
import com.my.pharmacy.dao.DealerDAOImpl;
import com.my.pharmacy.model.Dealer;
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

    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final ObservableList<Dealer> masterData = FXCollections.observableArrayList();

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
                String lowerCaseFilter = newVal.toLowerCase();

                return dealer.getCompanyName().toLowerCase().contains(lowerCaseFilter) ||
                        dealer.getLicenseNo().toLowerCase().contains(lowerCaseFilter) ||
                        dealer.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        dealerTable.setItems(filteredData);
    }

    @FXML
    private void handleSave() {
        if (companyField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert("Error", "Company Name and Phone are required.");
            return;
        }

        Dealer newDealer = new Dealer(
                0,
                nameField.getText(),
                companyField.getText(),
                phoneField.getText(),
                addressField.getText(),
                licenseField.getText()
        );

        dealerDAO.addDealer(newDealer);
        loadData(); // Refresh table
        clearFields();
        showAlert("Success", "Dealer registered successfully!");
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        companyField.clear();
        phoneField.clear();
        licenseField.clear();
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