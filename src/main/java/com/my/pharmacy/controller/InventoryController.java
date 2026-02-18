package com.my.pharmacy.controller;

import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InventoryController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colGeneric;
    @FXML private TableColumn<Product, String> colManufacturer;
    @FXML private TableColumn<Product, Integer> colPackSize;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGeneric.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        colManufacturer.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        colPackSize.setCellValueFactory(new PropertyValueFactory<>("packSize"));

        loadInventoryData();
    }

    private void loadInventoryData() {
        productList.clear();
        productList.addAll(productDAO.getAllProducts());
        inventoryTable.setItems(productList);
    }

    @FXML
    private void handleAddNewProduct() {
        String mode = com.my.pharmacy.util.ConfigUtil.getAppMode();
        String fxmlPath = mode.equals("WHOLESALE")
                ? "/fxml/AddProductWholesale.fxml"
                : "/fxml/AddProductRetail.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Product - " + mode);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the table after the dialog is closed
            loadInventoryData();
        } catch (IOException e) {
            System.err.println("Error loading dialog: " + fxmlPath);
            e.printStackTrace();
        }
    }


}