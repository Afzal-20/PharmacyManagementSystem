package com.my.pharmacy.controller;

import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.SaleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddToCartWholesaleController {

    @FXML private Label productNameLabel;
    @FXML private TextField qtyField, bonusField, discountField;

    private Batch selectedBatch;
    private SaleItem createdItem;
    private boolean confirmed = false;

    public void setBatchData(Batch batch) {
        this.selectedBatch = batch;
        this.productNameLabel.setText(batch.getProduct().getName());
    }

    @FXML
    private void handleSave() {
        try {
            int boxes = Integer.parseInt(qtyField.getText());
            int bonusUnits = Integer.parseInt(bonusField.getText());
            double discount = Double.parseDouble(discountField.getText());

            // Hybrid Math: Convert boxes to units
            int units = boxes * selectedBatch.getProduct().getPackSize();
            double unitPrice = selectedBatch.getTradePrice() / selectedBatch.getProduct().getPackSize();

            createdItem = new SaleItem(
                    selectedBatch.getProductId(),
                    selectedBatch.getBatchId(),
                    units,
                    unitPrice,
                    bonusUnits,
                    discount
            );
            createdItem.setProductName(selectedBatch.getProduct().getName());

            confirmed = true;
            closeWindow();
        } catch (NumberFormatException e) {
            // Add alert here if needed
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public SaleItem getCreatedItem() { return createdItem; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) qtyField.getScene().getWindow()).close(); }
}