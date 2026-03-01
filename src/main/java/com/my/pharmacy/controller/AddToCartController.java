package com.my.pharmacy.controller;

import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.SaleItem;
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
            if (qtyField.getText().isEmpty()) return;

            int boxes = Integer.parseInt(qtyField.getText());
            int bonusBoxes = bonusField.getText().isEmpty() ? 0 : Integer.parseInt(bonusField.getText());
            double discPercent = discountField.getText().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText());

            double unitPrice = selectedBatch.getTradePrice(); // Strict Box Rate

            if ((boxes + bonusBoxes) > selectedBatch.getQtyOnHand()) {
                System.err.println("Insufficient Stock!");
                return;
            }

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
            System.err.println("Invalid input numeric values.");
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public SaleItem getCreatedItem() { return createdItem; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) qtyField.getScene().getWindow()).close(); }
}