package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.CalculationEngine;
import com.my.pharmacy.util.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML private TextField nameField, genericField, manufacturerField, packSizeField;
    @FXML private TextField batchField, costField, marginField, qtyBoxesField, tradePriceField;
    @FXML private DatePicker expiryPicker;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO   batchDAO   = new BatchDAOImpl();

    @FXML
    public void initialize() {
        marginField.textProperty().addListener((obs, oldVal, newVal) -> calculateTradePrice());
        costField.textProperty().addListener((obs, oldVal, newVal)   -> calculateTradePrice());
    }

    private void calculateTradePrice() {
        try {
            double cost   = Double.parseDouble(costField.getText().trim());
            double margin = Double.parseDouble(marginField.getText().trim());
            double trade  = CalculationEngine.calculateTradePrice(cost, margin);
            tradePriceField.setText(String.valueOf(Math.round(trade)));
        } catch (NumberFormatException e) {
            tradePriceField.clear();
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty() || expiryPicker.getValue() == null) {
            NotificationService.warn("Medicine name and expiry date are required.");
            return;
        }
        try {
            int    packSize   = Integer.parseInt(packSizeField.getText().trim());
            int    totalBoxes = Integer.parseInt(qtyBoxesField.getText().trim());
            double cost       = Double.parseDouble(costField.getText().trim());
            double tradePrice = Double.parseDouble(tradePriceField.getText().trim());
            String expiryStr  = expiryPicker.getValue().toString();

            Product product = new Product(0, nameField.getText().trim(), genericField.getText().trim(),
                    manufacturerField.getText().trim(), "", packSize, 10, "");
            int productId = productDAO.addProduct(product);

            if (productId == -1) {
                NotificationService.error("Failed to save product. Please try again.");
                return;
            }

            Batch batch = new Batch(0, productId, batchField.getText().trim(), expiryStr,
                    totalBoxes, cost, tradePrice, 0.0, 0.0, 0.0);
            batchDAO.addBatch(batch);
            closeWindow();

        } catch (NumberFormatException e) {
            NotificationService.error("Please enter valid numbers for Pack Size, Quantity, and Prices.");
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}