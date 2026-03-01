package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class InventoryController {

    @FXML private TableView<Batch> inventoryTable;
    @FXML private TableColumn<Batch, String> colName;
    @FXML private TableColumn<Batch, String> colBatch;
    @FXML private TableColumn<Batch, String> colExpiry;
    @FXML private TableColumn<Batch, Integer> colPackSize;
    @FXML private TableColumn<Batch, Integer> colStock;
    @FXML private TableColumn<Batch, Double> colTradePrice;

    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final ObservableList<Batch> batchList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadInventoryData();
    }

    private void setupColumns() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colPackSize.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getProduct().getPackSize()));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colTradePrice.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));
    }

    private void loadInventoryData() {
        batchList.setAll(batchDAO.getAllBatches());
        inventoryTable.setItems(batchList);
    }

    @FXML
    private void handleAddNewProduct() {
        // Bypasses the mode check and loads the unified Wholesale FXML directly
        openDialog("/fxml/AddProduct.fxml", "Add New Product");
    }

    @FXML
    private void handleAdjustStock() {
        Batch selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a batch from the table to adjust.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockAdjustmentDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Adjust Stock: " + selected.getProduct().getName());
            stage.initModality(Modality.APPLICATION_MODAL);

            StockAdjustmentController controller = loader.getController();
            controller.setBatchData(selected);

            stage.showAndWait();
            loadInventoryData(); // Refresh table after dialog closes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadInventoryData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}