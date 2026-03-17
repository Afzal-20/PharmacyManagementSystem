package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.DialogUtil;
import com.my.pharmacy.util.CalculationEngine;
import com.my.pharmacy.util.NotificationService;
import com.my.pharmacy.util.TimeUtil;
import com.my.pharmacy.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PurchaseController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    @FXML private ComboBox<Dealer> dealerComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField batchNoField, qtyField, costField, marginField, tradeField, invoiceNoField;
    @FXML private TextField compDiscField, taxField;
    @FXML private DatePicker expiryPicker;

    private final DealerDAO  dealerDAO  = new DealerDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO   batchDAO   = new BatchDAOImpl();
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
        costField.textProperty().addListener((obs, oldVal, newVal)   -> calculateTradePrice());
    }

    private void calculateTradePrice() {
        try {
            double cost   = Double.parseDouble(costField.getText());
            double margin = Double.parseDouble(marginField.getText());
            double trade  = CalculationEngine.calculateTradePrice(cost, margin);
            tradeField.setText(String.valueOf(Math.round(trade)));
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
            loadDropdownData();
        } catch (IOException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
    }

    @FXML
    private void handleSavePurchase() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        Dealer  selectedDealer  = dealerComboBox.getSelectionModel().getSelectedItem();
        LocalDate expiryDate    = expiryPicker.getValue();

        // Validate dropdowns and date picker first
        if (selectedProduct == null || selectedDealer == null || expiryDate == null) {
            NotificationService.warn("Please select a Dealer, Product, and Expiry Date.");
            return;
        }

        // Validate all numeric fields with visual feedback via Validator
        List<String> errors = Validator.validate()
                .requirePositiveInt(qtyField,       "Quantity")
                .requirePositiveDouble(costField,   "Cost Price")
                .requirePositiveDouble(tradeField,  "Trade Price")
                .requireNonNegativeDouble(compDiscField, "Company Discount")
                .requireNonNegativeDouble(taxField,      "Sales Tax")
                .getErrors();

        if (!errors.isEmpty()) {
            NotificationService.error(errors.getFirst());
            return;
        }

        try {
            String rawBatch  = batchNoField.getText();
            String batchNo   = (rawBatch == null || rawBatch.trim().isEmpty())
                    ? "GEN-" + (System.currentTimeMillis() % 10000000) : rawBatch.trim();

            String rawInvoice = invoiceNoField.getText();
            String invoiceNo  = (rawInvoice == null || rawInvoice.trim().isEmpty()) ? "N/A" : rawInvoice.trim();

            String expiryStr  = expiryDate.toString();
            int    totalBoxes = Integer.parseInt(qtyField.getText().trim());
            double boxCost    = Double.parseDouble(costField.getText().trim());
            double boxTrade   = Double.parseDouble(tradeField.getText().trim());
            double compDisc   = Double.parseDouble(compDiscField.getText().trim());
            double salesTax   = Double.parseDouble(taxField.getText().trim());

            if (!DialogUtil.confirm("Confirm Purchase", "Proceed with this purchase?",
                    totalBoxes + " boxes of " + selectedProduct.getName() + " from " + selectedDealer.getName())) return;

            double netBoxCost           = CalculationEngine.calculateNetPurchaseCost(boxCost, compDisc, salesTax);
            double totalPayableToDealer = CalculationEngine.calculateTotalPayableToDealer(netBoxCost, totalBoxes);

            Payment purchaseLedgerEntry = new Payment(0, selectedDealer.getId(), "DEALER", totalPayableToDealer, "PURCHASE",
                    "Purchased " + totalBoxes + " boxes of " + selectedProduct.getName(),
                    TimeUtil.nowTimestamp());

            Batch existingBatch = batchDAO.getExactBatchMatch(selectedProduct.getId(), batchNo, expiryStr, boxCost, boxTrade);

            if (existingBatch != null) {
                existingBatch.setQtyOnHand(existingBatch.getQtyOnHand() + totalBoxes);
                existingBatch.setCompanyDiscount(compDisc);
                existingBatch.setSalesTax(salesTax);
                batchDAO.updateBatch(existingBatch);
                paymentDAO.recordPayment(purchaseLedgerEntry);
                batchDAO.recordPurchaseHistory(selectedDealer.getId(), selectedProduct.getId(),
                        selectedProduct.getName(), batchNo, invoiceNo, totalBoxes, boxCost, boxTrade);
                NotificationService.success("Stock merged successfully. Dealer account updated.");
                clearFields();
                return;
            }

            Batch newBatch = new Batch(0, selectedProduct.getId(), batchNo, expiryStr,
                    totalBoxes, boxCost, boxTrade, 0.0, compDisc, salesTax);
            batchDAO.addBatch(newBatch);
            paymentDAO.recordPayment(purchaseLedgerEntry);
            batchDAO.recordPurchaseHistory(selectedDealer.getId(), selectedProduct.getId(),
                    selectedProduct.getName(), batchNo, invoiceNo, totalBoxes, boxCost, boxTrade);
            NotificationService.success("Purchased " + totalBoxes + " boxes. Dealer account updated.");
            clearFields();

        } catch (NumberFormatException e) {
            // Validator guards above prevent this from ever being reached —
            // kept as a safety net only
            NotificationService.error("Please enter valid numeric values for Quantity and Prices.");
        }
    }

    @FXML
    private void clearFields() {
        batchNoField.clear(); qtyField.clear(); costField.clear(); marginField.clear();
        tradeField.clear(); invoiceNoField.clear(); expiryPicker.setValue(null);
        compDiscField.setText("0.0"); taxField.setText("0.0");
    }
}