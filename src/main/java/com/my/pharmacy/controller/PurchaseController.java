package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.IOException;
import java.time.LocalDate;

public class PurchaseController {

    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField batchNoField, qtyField, costField, marginField, tradeField, invoiceNoField;
    @FXML private TextField compDiscField, taxField;
    @FXML private DatePicker expiryPicker;

    private final DealerDAO dealerDAO = new DealerDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    @FXML
    public void initialize() {
        loadDropdownData();
        setupConverters();
        setupMarginListener();
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

    private void setupMarginListener() {
        marginField.textProperty().addListener((obs, oldVal, newVal) -> calculateTradePrice());
        costField.textProperty().addListener((obs, oldVal, newVal) -> calculateTradePrice());
    }

    private void calculateTradePrice() {
        try {
            double cost = Double.parseDouble(costField.getText());
            double margin = Double.parseDouble(marginField.getText());
            double trade = cost + (cost * (margin / 100.0));
            tradeField.setText(String.valueOf(Math.round(trade))); // Rounds to nearest Rupee
        } catch (NumberFormatException e) {
            tradeField.clear();
        }
    }

    @FXML
    private void openMinimalAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MinimalAddProduct.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadDropdownData(); // Refresh list after closing
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSavePurchase() {
        try {
            Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
            Dealer selectedDealer = dealerComboBox.getSelectionModel().getSelectedItem();
            LocalDate expiryDate = expiryPicker.getValue();

            if (selectedProduct == null || selectedDealer == null || expiryDate == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required dropdowns and select an Expiry Date.");
                return;
            }

            String batchNo = batchNoField.getText();
            String expiryStr = expiryDate.toString(); // YYYY-MM-DD
            String invoiceNo = invoiceNoField.getText() == null ? "N/A" : invoiceNoField.getText();

            int totalBoxes = Integer.parseInt(qtyField.getText());
            double boxCost = Double.parseDouble(costField.getText());
            double boxTrade = Double.parseDouble(tradeField.getText());
            double compDisc = Double.parseDouble(compDiscField.getText());
            double salesTax = Double.parseDouble(taxField.getText());

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to proceed with this purchase?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            double netBoxCost = com.my.pharmacy.util.CalculationEngine.calculateNetPurchaseCost(boxCost, compDisc, salesTax);
            double totalPayableToDealer = netBoxCost * totalBoxes;

            Payment purchaseLedgerEntry = new Payment(0, selectedDealer.getId(), "DEALER", totalPayableToDealer, "PURCHASE",
                    "Purchased " + totalBoxes + " boxes of " + selectedProduct.getName(), new java.sql.Timestamp(System.currentTimeMillis()));


            Batch existingBatch = batchDAO.getExactBatchMatch(selectedProduct.getId(), batchNo, expiryStr, boxCost, boxTrade);

            if (existingBatch != null) {
                existingBatch.setQtyOnHand(existingBatch.getQtyOnHand() + totalBoxes);
                existingBatch.setCompanyDiscount(compDisc);
                existingBatch.setSalesTax(salesTax);

                batchDAO.updateBatch(existingBatch);
                paymentDAO.recordPayment(purchaseLedgerEntry);
                batchDAO.recordPurchaseHistory(selectedDealer.getId(), selectedProduct.getId(), selectedProduct.getName(), batchNo, invoiceNo, totalBoxes, boxCost, boxTrade);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Stock merged perfectly. Dealer account updated.");
                clearFields();
                return;
            }

            // Passing 0.0 for Retail Price temporarily until Phase 5
            Batch newBatch = new Batch(0, selectedProduct.getId(), batchNo, expiryStr, totalBoxes, boxCost, boxTrade, 0.0, compDisc, salesTax);
            batchDAO.addBatch(newBatch);
            paymentDAO.recordPayment(purchaseLedgerEntry);
            batchDAO.recordPurchaseHistory(selectedDealer.getId(), selectedProduct.getId(), selectedProduct.getName(), batchNo, invoiceNo, totalBoxes, boxCost, boxTrade);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Purchased " + totalBoxes + " boxes. Dealer account updated.");
            clearFields();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for Quantity and Prices.");
        }
    }

    @FXML
    private void clearFields() {
        batchNoField.clear(); qtyField.clear(); costField.clear(); marginField.clear();
        tradeField.clear(); invoiceNoField.clear(); expiryPicker.setValue(null);
        compDiscField.setText("0.0"); taxField.setText("0.0");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}