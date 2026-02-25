package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
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
        lblCurrentBoxes.setText(String.valueOf(batch.getBoxCount()));
    }

    @FXML
    private void handleUpdate() {
        try {
            int newBoxes = Integer.parseInt(newBoxesField.getText());

            // Box-to-Unit Math
            int packSize = selectedBatch.getProduct().getPackSize();
            int newTotalUnits = newBoxes * packSize;

            selectedBatch.setQtyOnHand(newTotalUnits);
            batchDAO.updateBatch(selectedBatch);

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