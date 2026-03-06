package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML private TextField nameField, genericField, manufacturerField, packSizeField;
    @FXML private TextField batchField, costField, marginField, qtyBoxesField, tradePriceField;
    @FXML private DatePicker expiryPicker;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();

    @FXML
    public void initialize() {
        // Auto-calculate Trade Price based on Cost and Margin
        marginField.textProperty().addListener((obs, oldVal, newVal) -> calculateTradePrice());
        costField.textProperty().addListener((obs, oldVal, newVal) -> calculateTradePrice());
    }

    private void calculateTradePrice() {
        try {
            double cost = Double.parseDouble(costField.getText().trim());
            double margin = Double.parseDouble(marginField.getText().trim());
            double trade = cost + (cost * (margin / 100.0));
            tradePriceField.setText(String.valueOf(Math.round(trade))); // Rounds to nearest Rupee
        } catch (NumberFormatException e) {
            tradePriceField.clear(); // Clear if input is invalid/empty
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty() || expiryPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Medicine name and expiry date are required.");
            return;
        }

        try {
            int packSize = Integer.parseInt(packSizeField.getText().trim());
            int totalBoxes = Integer.parseInt(qtyBoxesField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());
            double tradePrice = Double.parseDouble(tradePriceField.getText().trim());
            String expiryStr = expiryPicker.getValue().toString();

            Product product = new Product(0, nameField.getText().trim(), genericField.getText().trim(),
                    manufacturerField.getText().trim(), "", packSize, 10, "");

            int productId = productDAO.addProduct(product);

            if (productId == -1) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save product.");
                return;
            }

            Batch batch = new Batch(0, productId, batchField.getText().trim(), expiryStr,
                    totalBoxes, cost, tradePrice, 0.0, 0.0, 0.0);

            batchDAO.addBatch(batch);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for Pack Size, Quantity, and Prices.");
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}