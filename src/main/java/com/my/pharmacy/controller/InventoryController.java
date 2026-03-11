package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.NotificationService;
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
    @FXML private TableColumn<Batch, String>  colName, colBatch, colExpiry;
    @FXML private TableColumn<Batch, Integer> colPackSize, colStock;
    @FXML private TableColumn<Batch, Double>  colTradePrice;
    @FXML private Button btnAdjustStock, btnEditProduct;

    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final ObservableList<Batch> batchList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadInventoryData();
        boolean isAdmin = com.my.pharmacy.util.UserSession.getInstance() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser().isAdmin();
        btnAdjustStock.setVisible(isAdmin); btnAdjustStock.setManaged(isAdmin);
        btnEditProduct.setVisible(isAdmin); btnEditProduct.setManaged(isAdmin);
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
        openDialog("/fxml/AddProduct.fxml", "Add New Product");
    }

    @FXML
    private void handleAdjustStock() {
        Batch selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationService.warn("Please select a batch to adjust.");
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
            loadInventoryData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleEditProduct() {
        Batch selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationService.warn("Please select a batch to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProductDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Product: " + selected.getProduct().getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            EditProductController controller = loader.getController();
            controller.setProductData(selected.getProduct());
            stage.showAndWait();
            loadInventoryData();
        } catch (IOException e) { e.printStackTrace(); }
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
        } catch (IOException e) { e.printStackTrace(); }
    }
}
