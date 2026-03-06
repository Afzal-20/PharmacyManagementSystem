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
    @FXML private TextField batchField, costField;
    @FXML private DatePicker expiryPicker; // FIX #3: Was a free-text TextField — malformed dates broke expiry checks

    @FXML private TextField qtyBoxesField;
    @FXML private TextField tradePriceField;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();

    @FXML
    private void handleSave() {
        // FIX #8: Validate inputs and show Alerts instead of printing to System.err
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Medicine name is required.");
            return;
        }

        if (expiryPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select an expiry date.");
            return;
        }

        try {
            int packSize = Integer.parseInt(packSizeField.getText().trim());
            int totalBoxes = Integer.parseInt(qtyBoxesField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());
            double tradePrice = Double.parseDouble(tradePriceField.getText().trim());
            String expiryStr = expiryPicker.getValue().toString(); // Always valid YYYY-MM-DD

            Product product = new Product(0, nameField.getText().trim(), genericField.getText().trim(),
                    manufacturerField.getText().trim(), "", packSize, 10, "");

            int productId = productDAO.addProduct(product);

            if (productId == -1) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save product. Please try again.");
                return;
            }

            Batch batch = new Batch(0, productId, batchField.getText().trim(), expiryStr,
                    totalBoxes, cost, tradePrice, 0.0, 0.0, 0.0);

            batchDAO.addBatch(batch);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for Pack Size, Quantity, Cost, and Trade Price.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}