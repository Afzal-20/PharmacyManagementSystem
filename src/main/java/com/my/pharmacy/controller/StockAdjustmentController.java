package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StockAdjustmentController {

    @FXML private Label lblMedicineName;
    @FXML private Label lblBatchNo;
    @FXML private Label lblCurrentBoxes;
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
                showAlert("Invalid Input", "Stock quantity cannot be negative.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Change stock from " + selectedBatch.getQtyOnHand() + " to " + newBoxes + " boxes?");
            if (confirm.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) != javafx.scene.control.ButtonType.OK) return;

            // FIX #2: Capture old qty BEFORE any change, then call adjustStockWithAudit()
            // so every manual adjustment is written to stock_adjustments_audit.
            int oldQty = selectedBatch.getQtyOnHand();
            int userId = UserSession.getInstance().getUser().getId();

            batchDAO.adjustStockWithAudit(selectedBatch.getId(), oldQty, newBoxes, "Manual Adjustment", userId);

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid numeric value for the box count.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) newBoxesField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}