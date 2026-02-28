package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import java.util.Optional;

public class PurchaseController {

    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField batchNoField, expiryField, qtyField, costField, tradeField, retailField;
    @FXML private TextField compDiscField, taxField;

    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

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
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select both a Dealer and a Product.");
                return;
            }

            String batchNo = batchNoField.getText();
            String expiry = expiryField.getText();

            // PURE BOX-CENTRIC MATH
            int totalBoxes = Integer.parseInt(qtyField.getText());
            double boxCost = Double.parseDouble(costField.getText());
            double boxTrade = Double.parseDouble(tradeField.getText());
            double boxRetail = Double.parseDouble(retailField.getText());

            double compDisc = Double.parseDouble(compDiscField.getText());
            double salesTax = Double.parseDouble(taxField.getText());

            // --- DEALER KHATA MATH ---
            // Calculate the exact net cost of one box, then multiply by total boxes purchased
            double netBoxCost = com.my.pharmacy.util.CalculationEngine.calculateNetPurchaseCost(boxCost, compDisc, salesTax);
            double totalPayableToDealer = netBoxCost * totalBoxes;

            // Log the purchase to the Dealer's Khata Ledger
            Payment purchaseLedgerEntry = new Payment(0, selectedDealer.getId(), "DEALER", totalPayableToDealer, "PURCHASE",
                    "Purchased " + totalBoxes + " boxes of " + selectedProduct.getName(), new java.sql.Timestamp(System.currentTimeMillis()));

            Batch existingBatch = batchDAO.getExactBatchMatch(selectedProduct.getId(), batchNo, expiry, boxCost, boxTrade, boxRetail);

            if (existingBatch != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exact Batch Found");
                alert.setHeaderText("An exact match for Batch '" + batchNo + "' was found in your inventory.");
                alert.setContentText("Do you want to merge these " + totalBoxes + " boxes into the existing stock?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    existingBatch.setQtyOnHand(existingBatch.getQtyOnHand() + totalBoxes);
                    existingBatch.setCompanyDiscount(compDisc);
                    existingBatch.setSalesTax(salesTax);

                    batchDAO.updateBatch(existingBatch);
                    paymentDAO.recordPayment(purchaseLedgerEntry); // Updates Khata & History

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Stock merged perfectly. Dealer account updated by Rs. " + String.format("%.2f", totalPayableToDealer));
                    clearFields();
                }
                return;
            }

            Batch newBatch = new Batch(0, selectedProduct.getId(), batchNo, expiry, totalBoxes, boxCost, boxTrade, boxRetail, 0.0, compDisc, salesTax);
            batchDAO.addBatch(newBatch);
            paymentDAO.recordPayment(purchaseLedgerEntry); // Updates Khata & History

            showAlert(Alert.AlertType.INFORMATION, "Success", "Purchased " + totalBoxes + " boxes. Dealer account updated by Rs. " + String.format("%.2f", totalPayableToDealer));
            clearFields();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for Quantity and Prices.");
        }
    }

    @FXML
    private void clearFields() {
        batchNoField.clear();
        expiryField.clear();
        qtyField.clear();
        costField.clear();
        tradeField.clear();
        retailField.clear();
        compDiscField.setText("0.0");
        taxField.setText("0.0");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}