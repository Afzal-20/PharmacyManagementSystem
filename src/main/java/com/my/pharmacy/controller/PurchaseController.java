package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class PurchaseController {

    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField batchNoField, expiryField, qtyField, costField, tradeField;

    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();

    @FXML
    public void initialize() {
        loadDropdownData();
        setupConverters();
    }

    private void loadDropdownData() {
        dealerComboBox.setItems(FXCollections.observableArrayList(dealerDAO.getAllDealers()));
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
    }

    private void setupConverters() {
        dealerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Dealer d) { return d == null ? "" : d.getName() + " (" + d.getCompanyName() + ")"; }
            @Override public Dealer fromString(String s) { return null; }
        });

        productComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName(); }
            @Override public Product fromString(String s) { return null; }
        });
    }

    @FXML
    private void handleSavePurchase() {
        try {
            Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
            Dealer selectedDealer = dealerComboBox.getSelectionModel().getSelectedItem();

            if (selectedProduct == null || selectedDealer == null) {
                showAlert("Validation Error", "Please select both a Dealer and a Product.");
                return;
            }

            // Calculations
            int boxes = Integer.parseInt(qtyField.getText());
            int totalUnits = boxes * selectedProduct.getPackSize();

            // Note: We store Unit Prices in the DB for easier POS math later
            double unitCost = Double.parseDouble(costField.getText()) / selectedProduct.getPackSize();
            double unitTrade = Double.parseDouble(tradeField.getText()) / selectedProduct.getPackSize();
            double unitRetail = unitTrade * 1.15; // Default 15% margin for retail if not specified

            Batch newBatch = new Batch(
                    0,
                    selectedProduct.getId(),
                    batchNoField.getText(),
                    expiryField.getText(),
                    totalUnits,
                    unitCost,
                    unitTrade,
                    unitRetail,
                    0.0 // Discount percent
            );

            batchDAO.addBatch(newBatch);

            showAlert("Success", "Stock updated! Total Units added: " + totalUnits);
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numeric values for Quantity and Prices.");
        }
    }

    private void clearFields() {
        batchNoField.clear();
        expiryField.clear();
        qtyField.clear();
        costField.clear();
        tradeField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}