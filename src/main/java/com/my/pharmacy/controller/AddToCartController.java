package com.my.pharmacy.controller;

import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.SaleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddToCartController {

    @FXML private Label productNameLabel;
    @FXML private TextField qtyField, bonusField, discountField;

    private Batch selectedBatch;
    private SaleItem createdItem;
    private boolean confirmed = false;

    public void setBatchData(Batch batch) {
        this.selectedBatch = batch;
        this.productNameLabel.setText(batch.getProduct().getName() + " (Batch: " + batch.getBatchNo() + ")");
    }

    @FXML
    private void handleSave() {
        try {
            // 1. Input Validation
            if (qtyField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a quantity.");
                return;
            }

            // --- NEW: EXPIRY HARD STOP ---
            try {
                java.time.LocalDate expiry = java.time.LocalDate.parse(selectedBatch.getExpiryDate());
                if (expiry.isBefore(java.time.LocalDate.now())) {
                    showAlert(Alert.AlertType.ERROR, "Critical Error: Expired Stock",
                            "This batch expired on " + expiry + ". Selling expired medicine is prohibited. Please select a different batch.");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not parse expiry date for batch " + selectedBatch.getBatchNo());
            }
            // -----------------------------

            int boxes = Integer.parseInt(qtyField.getText().trim());
            int bonusBoxes = bonusField.getText().trim().isEmpty() ? 0 : Integer.parseInt(bonusField.getText().trim());
            double discPercent = discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim());

            double unitPrice = selectedBatch.getTradePrice(); // Strict Box Rate

            // 2. Business Logic Validation (Stock Check)
            int totalRequested = boxes + bonusBoxes;
            if (totalRequested > selectedBatch.getQtyOnHand()) {
                showAlert(Alert.AlertType.ERROR, "Insufficient Stock",
                        "You requested " + totalRequested + " boxes, but " + selectedBatch.getQtyOnHand() + " are available in this batch.");
                return;
            }

            if (boxes <= 0 && bonusBoxes <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity cannot be zero.");
                return;
            }

            // 3. Create Item
            createdItem = new SaleItem(
                    selectedBatch.getProductId(),
                    selectedBatch.getId(),
                    boxes,
                    unitPrice,
                    bonusBoxes,
                    discPercent
            );
            createdItem.setProductName(selectedBatch.getProduct().getName());

            confirmed = true;
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numeric values for Quantity and Discount.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConfirmed() { return confirmed; }

    public SaleItem getCreatedItem() { return createdItem; }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (qtyField.getScene() != null && qtyField.getScene().getWindow() != null) {
            ((Stage) qtyField.getScene().getWindow()).close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}