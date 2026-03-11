package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.DialogUtil;
import com.my.pharmacy.util.NotificationService;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StockAdjustmentController {

    @FXML private Label lblMedicineName, lblBatchNo, lblCurrentBoxes;
    @FXML private TextField newBoxesField;

    private Batch selectedBatch;
    private final BatchDAO batchDAO = new BatchDAOImpl();

    public void setBatchData(Batch batch) {
        this.selectedBatch = batch;
        lblMedicineName.setText(batch.getProduct().getName());
        lblBatchNo.setText(batch.getBatchNo());
        lblCurrentBoxes.setText(String.valueOf(batch.getQtyOnHand()));
    }

    @FXML
    private void handleUpdate() {
        try {
            int newBoxes = Integer.parseInt(newBoxesField.getText().trim());
            if (newBoxes < 0) {
                NotificationService.warn("Stock quantity cannot be negative.");
                return;
            }
            if (!DialogUtil.confirm("Confirm Stock Adjustment",
                    "Change stock from " + selectedBatch.getQtyOnHand() + " → " + newBoxes + " boxes?",
                    "An audit record will be saved for this change.")) return;

            int oldQty = selectedBatch.getQtyOnHand();
            int userId = UserSession.getInstance().getUser().getId();
            batchDAO.adjustStockWithAudit(selectedBatch.getId(), oldQty, newBoxes, "Manual Adjustment", userId);
            closeWindow();
        } catch (NumberFormatException e) {
            NotificationService.error("Please enter a valid numeric value for the box count.");
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) newBoxesField.getScene().getWindow()).close(); }
}
