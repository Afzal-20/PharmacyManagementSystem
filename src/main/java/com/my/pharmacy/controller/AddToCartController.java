package com.my.pharmacy.controller;

import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.SaleItem;
import com.my.pharmacy.util.NotificationService;
import javafx.fxml.FXML;
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
            if (qtyField.getText().trim().isEmpty()) {
                NotificationService.warn("Please enter a quantity.");
                return;
            }

            // Expiry hard stop
            try {
                java.time.LocalDate expiry = java.time.LocalDate.parse(selectedBatch.getExpiryDate());
                if (expiry.isBefore(java.time.LocalDate.now())) {
                    NotificationService.error("Batch expired on " + expiry + ". Selling expired medicine is prohibited.");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not parse expiry date for batch " + selectedBatch.getBatchNo());
            }

            int    boxes      = Integer.parseInt(qtyField.getText().trim());
            int    bonusBoxes = bonusField.getText().trim().isEmpty() ? 0 : Integer.parseInt(bonusField.getText().trim());
            double discPercent = discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim());
            double unitPrice   = selectedBatch.getTradePrice();

            int totalRequested = boxes + bonusBoxes;
            if (totalRequested > selectedBatch.getQtyOnHand()) {
                NotificationService.error("Requested " + totalRequested + " boxes but only " + selectedBatch.getQtyOnHand() + " available.");
                return;
            }
            if (boxes <= 0 && bonusBoxes <= 0) {
                NotificationService.warn("Quantity cannot be zero.");
                return;
            }

            createdItem = new SaleItem(selectedBatch.getProductId(), selectedBatch.getId(),
                    boxes, unitPrice, bonusBoxes, discPercent);
            createdItem.setProductName(selectedBatch.getProduct().getName());

            confirmed = true;
            closeWindow();

        } catch (NumberFormatException e) {
            NotificationService.error("Please enter valid numeric values for Quantity and Discount.");
        } catch (Exception e) {
            NotificationService.error("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConfirmed()    { return confirmed; }
    public SaleItem getCreatedItem() { return createdItem; }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        if (qtyField.getScene() != null && qtyField.getScene().getWindow() != null) {
            ((Stage) qtyField.getScene().getWindow()).close();
        }
    }
}
